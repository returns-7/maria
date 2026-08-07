package com.app.maria.domain.account.service;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.dto.AccountStatusLogDTO;
import com.app.maria.domain.account.dto.request.AccountRequestDTO;
import com.app.maria.domain.account.dto.request.AccountReapplyRequestDTO;
import com.app.maria.domain.account.dto.response.AccountLogResponseDTO;
import com.app.maria.domain.account.dto.response.AccountResponseDTO;
import com.app.maria.domain.account.dto.response.MydataRiaAccountsResponseDTO;
import com.app.maria.domain.account.exception.AccountException;
import com.app.maria.domain.account.exception.AccountNotFoundException;
import com.app.maria.domain.account.exception.DuplicateAccountException;
import com.app.maria.domain.account.exception.InvalidAccountRequestException;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.account.mapper.AccountStatusLogMapper;
import com.app.maria.domain.account.type.AutomaticRejectionReason;
import com.app.maria.domain.account.type.Status;
import com.app.maria.global.clock.service.BusinessClockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private static final BigDecimal MAX_LIMIT_AMOUNT = BigDecimal.valueOf(50_000_000L);
  private static final BigDecimal MIN_LIMIT_AMOUNT = BigDecimal.ONE;
  private static final LocalDate RIA_APPLICATION_START_DATE = LocalDate.of(2026, 3, 23);
  private static final LocalDate RIA_APPLICATION_END_DATE = LocalDate.of(2026, 12, 31);
  private static final int ACCOUNT_NO_RETRY_LIMIT = 5;
  private static final long ACCOUNT_NO_MIN = 1_000_000_000L;
  private static final long ACCOUNT_NO_MAX_EXCLUSIVE = 10_000_000_000L;
  private static final String LIMIT_CHANGE_REASON_PREFIX = "LIMIT_CHANGE|from=";

  private final AccountMapper accountMapper;
  private final AccountStatusLogMapper accountStatusLogMapper;
  private final BusinessClockService businessClockService;

  private final RestClient restClient;

  @Value("${custom.mydata.url}")
  private String myDataUrl;
  @Value("${custom.mydata.own-broker-name}")
  private String ownBrokerName;


  @Override
  public List<AccountResponseDTO> findAll() {
    return accountMapper.selectAllAccount().stream().map(AccountResponseDTO::new).toList();
  }

  @Override
  public BigDecimal getAvailableLimit(Long customerId) {
    validateCustomerExists(customerId);
    return calculateAvailableLimit(customerId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO updateAccountLimit(AccountRequestDTO accountRequestDTO) {
    AccountDTO accountDTO = accountRequestDTO.toAccountDTO();
    validateCustomerExists(accountDTO.getCustomerId());
    Long customerId = accountDTO.getCustomerId();
    BigDecimal newLimitAmount = accountDTO.getLimitAmount();

    AccountDTO foundAccount = accountMapper.selectByCustomerId(customerId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    if (foundAccount.getStatus() != Status.APPLIED && foundAccount.getStatus() != Status.OPENED) {
      throw new InvalidAccountRequestException("신청 또는 개설 상태의 계좌만 한도를 변경할 수 있습니다.");
    }

    validateLimitInput(newLimitAmount);
    validateLimitAvailability(newLimitAmount, calculateAvailableLimit(customerId));
    if (foundAccount.getLimitAmount().compareTo(newLimitAmount) == 0) {
      throw new InvalidAccountRequestException("기존 한도와 다른 금액을 입력해야 합니다.");
    }

    if (accountMapper.updateLimit(foundAccount.getAccountId(), foundAccount.getStatus(), foundAccount.getLimitAmount(), newLimitAmount) != 1) {
      throw new InvalidAccountRequestException("계좌 한도 변경 중 상태 또는 한도가 변경되었습니다.");
    }

    AccountStatusLogDTO limitChangeLog = AccountStatusLogDTO.builder()
        .accountId(foundAccount.getAccountId())
        .prevStatus(foundAccount.getStatus())
        .newStatus(foundAccount.getStatus())
        .changedAt(businessClockService.now())
        .reason(buildLimitChangeReason(foundAccount.getLimitAmount(), newLimitAmount))
        .build();

    if (accountStatusLogMapper.insertLog(limitChangeLog) != 1) {
      throw new AccountException("계좌 한도 변경 로그 생성 실패");
    }

    AccountDTO updatedAccount = accountMapper.selectByAccountId(foundAccount.getAccountId()).orElseThrow(() -> new AccountException("계좌 한도 변경 후 재조회 실패"));
    if (updatedAccount.getLimitAmount().compareTo(newLimitAmount) != 0) {
      throw new AccountException("계좌 한도 변경 결과 불일치");
    }

    if(!addAccountToMydata(updatedAccount)){
      throw new RuntimeException("마이데이터 등록 실패");
    }
    return new AccountResponseDTO(updatedAccount);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO applyAccount(AccountRequestDTO requestDTO) {
    Long customerId = requestDTO.getCustomerId();
    validateCustomerExists(customerId);
    LocalDateTime appliedAt = getApplicationTime();

    if (accountMapper.existsByCustomerId(customerId)) {
      throw new DuplicateAccountException("사용자의 기존 계좌 정보가 있습니다.");
    }

    AccountDTO account = requestDTO.toAccountDTO();
    account.setCreatedAt(appliedAt);

    validateLimitAvailability(account.getLimitAmount(), calculateAvailableLimit(customerId));
    Optional<AutomaticRejectionReason> rejectionReason = findAutomaticRejectionReason(customerId);

    try {
      if (accountMapper.insertApplication(account) < 1) {
        throw new AccountException("계좌 신청 등록 실패");
      }
    } catch (DuplicateKeyException e) {
      throw new DuplicateAccountException("사용자의 기존 계좌 정보가 있습니다.");
    }

    AccountDTO appliedAccount = accountMapper.selectByCustomerId(customerId).orElseThrow(() -> new AccountException("계좌 생성 후 재조회 실패"));
    AccountStatusLogDTO applicationLog = AccountStatusLogDTO.builder()
        .accountId(appliedAccount.getAccountId())
        .prevStatus(null)
        .newStatus(appliedAccount.getStatus())
        .changedAt(appliedAt)
        .reason("최초 개설 신청")
        .build();

    if (accountStatusLogMapper.insertLog(applicationLog) < 1) {
      throw new AccountException("로그 등록 실패");
    }

    return completeAutomaticDecision(appliedAccount, appliedAt, rejectionReason);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO approveAccount(Long accountId) {
    AccountDTO account = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));

    LocalDateTime openedAt = getApplicationTime();
    validateLimitAvailability(account.getLimitAmount(), calculateAvailableLimit(account.getCustomerId()));

    AccountStatusLogDTO log = AccountStatusLogDTO.builder()
        .accountId(account.getAccountId())
        .prevStatus(account.getStatus())
        .changedAt(openedAt)
        .reason("사용자 계좌 개설")
        .build();

    account.setOpenedAt(openedAt);
    openAccountWithRetry(account);

    AccountDTO openedAccount = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountException("계좌 개설 후 재조회 실패"));
    if (openedAccount.getStatus() != Status.OPENED) {
      throw new AccountException("상태 변경 실패");
    }
    return new AccountResponseDTO(saveStatusLog(openedAccount, log));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO rejectAccount(Long accountId, String reason) {
    AccountDTO account = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    String normalizedReason = normalizeReason(reason);

    AccountStatusLogDTO log = AccountStatusLogDTO.builder()
        .accountId(account.getAccountId())
        .prevStatus(account.getStatus())
        .changedAt(businessClockService.now())
        .reason(normalizedReason)
        .build();

    if (accountMapper.reject(account) < 1) {
      throw new InvalidAccountRequestException("사용자 계좌 신청 반려 실패");
    }

    AccountDTO rejectedAccount = accountMapper.selectByAccountId(accountId)
        .orElseThrow(() -> new AccountException("계좌 상태 변경 후 재조회 실패"));
    if (rejectedAccount.getStatus() != Status.REJECTED) {
      throw new AccountException("상태 변경 실패");
    }
    return new AccountResponseDTO(saveStatusLog(rejectedAccount, log));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO reapplyAccountByAccountId(Long accountId, AccountReapplyRequestDTO requestDTO) {
    AccountDTO foundAccount = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    LocalDateTime appliedAt = getApplicationTime();

    AccountDTO reapplication = requestDTO.toAccountDTO();
    reapplication.setAccountId(accountId);

    if (reapplication.getLimitAmount() == null) {
      reapplication.setLimitAmount(foundAccount.getLimitAmount());
    }

    validateLimitAvailability(reapplication.getLimitAmount(),calculateAvailableLimit(foundAccount.getCustomerId()));

    AccountStatusLogDTO log = AccountStatusLogDTO.builder()
        .accountId(accountId)
        .prevStatus(foundAccount.getStatus())
        .changedAt(appliedAt)
        .reason("사용자 계좌 개설 재신청")
        .build();

    if (accountMapper.reapply(reapplication) < 1) {
      throw new InvalidAccountRequestException("사용자 계좌 재신청 실패");
    }

    AccountDTO reappliedAccount = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountException("계좌 상태 변경 후 재조회 실패"));
    if (reappliedAccount.getStatus() != Status.APPLIED) {
      throw new AccountException("상태 변경 실패");
    }
    AccountDTO accountDTO = saveStatusLog(reappliedAccount, log);
    return new AccountResponseDTO(accountDTO);
  }

  @Override
  public AccountResponseDTO getAccountByAccountId(Long accountId) {
    AccountDTO account = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    return new AccountResponseDTO(account);
  }

  @Override
  public List<AccountLogResponseDTO> getStatusLogsByAccountId(Long accountId) {
    accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    return accountStatusLogMapper.selectByAccountId(accountId).stream().map(AccountLogResponseDTO::new).toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AccountResponseDTO overrideAccount(Long accountId, String reason) {
    String normalizedReason = normalizeReason(reason);
    AccountDTO account = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountNotFoundException("계좌 조회 실패"));
    if (account.getStatus() != Status.REJECTED) {
      throw new InvalidAccountRequestException("반려 상태의 계좌만 오버라이드할 수 있습니다.");
    }

    LocalDateTime openedAt = getApplicationTime();
    validateLimitAvailability(account.getLimitAmount(), calculateAvailableLimit(account.getCustomerId()));

    AccountStatusLogDTO log = AccountStatusLogDTO.builder()
        .accountId(accountId)
        .prevStatus(account.getStatus())
        .changedAt(openedAt)
        .reason(normalizedReason)
        .build();

    account.setOpenedAt(openedAt);
    openAccountWithRetry(account);

    AccountDTO openedAccount = accountMapper.selectByAccountId(accountId).orElseThrow(() -> new AccountException("계좌 오버라이드 후 재조회 실패"));
    if (openedAccount.getStatus() != Status.OPENED) {
      throw new AccountException("계좌 오버라이드 상태 변경 실패");
    }
    return new AccountResponseDTO(saveStatusLog(openedAccount, log));
  }

  private AccountResponseDTO completeAutomaticDecision(AccountDTO appliedAccount, LocalDateTime changedAt, Optional<AutomaticRejectionReason> rejectionReason) {
    if (appliedAccount.getStatus() != Status.APPLIED) {
      throw new AccountException("자동 판정할 수 없는 계좌 상태입니다.");
    }

    AccountStatusLogDTO log = AccountStatusLogDTO.builder()
        .accountId(appliedAccount.getAccountId())
        .prevStatus(Status.APPLIED)
        .changedAt(changedAt)
        .reason(rejectionReason.map(AutomaticRejectionReason::toLogReason).orElse("자동 판정 승인"))
        .build();

    if (rejectionReason.isPresent()) {
      if (accountMapper.reject(appliedAccount) < 1) {
        throw new AccountException("계좌 자동 반려 처리 실패");
      }
      AccountDTO rejectedAccount = accountMapper.selectByAccountId(appliedAccount.getAccountId()).orElseThrow(() -> new AccountException("자동 반려 후 계좌 재조회 실패"));
      if (rejectedAccount.getStatus() != Status.REJECTED) {
        throw new AccountException("자동 반려 상태 변경 실패");
      }
      AccountDTO accountDTO = saveStatusLog(rejectedAccount, log);
      addAccountToMydata(accountDTO);
      return new AccountResponseDTO(accountDTO);
    }

    appliedAccount.setOpenedAt(changedAt);
    openAccountWithRetry(appliedAccount);
    AccountDTO openedAccount = accountMapper.selectByAccountId(appliedAccount.getAccountId()).orElseThrow(() -> new AccountException("자동 승인 후 계좌 재조회 실패"));
    if (openedAccount.getStatus() != Status.OPENED) {
      throw new AccountException("자동 승인 상태 변경 실패");
    }
    AccountDTO loggedAccountDTO = saveStatusLog(openedAccount, log);
    if(!addAccountToMydata(loggedAccountDTO)){
      throw new RuntimeException("마이데이터 등록 실패");
    }
    return new AccountResponseDTO(loggedAccountDTO);
  }

  private AccountDTO saveStatusLog(AccountDTO account, AccountStatusLogDTO log) {
    log.setNewStatus(account.getStatus());
    if (accountStatusLogMapper.insertLog(log) < 1) {
      throw new AccountException("로그 생성 실패");
    }

    AccountStatusLogDTO savedLog = accountStatusLogMapper.selectLatestByAccountId(account.getAccountId()).orElseThrow(() -> new AccountException("상태 변경 후 로그 재조회 실패"));
    if (savedLog.getNewStatus() != account.getStatus()) {
      throw new AccountException("상태 변경 로그 불일치");
    }
    return account;
  }

  private BigDecimal calculateAvailableLimit(Long customerId) {
    Map<String, String> req = new HashMap<>();
    String ciHash = accountMapper.selectCiHashByCustomerId(customerId);
    System.out.println("ciHash: " + ciHash);
    if(ciHash.isBlank()){
      // TODO customer 도메인 제작 완료 후 수정
      // 추후 CustomerNotFoundException으로 변경 예정
      throw new AccountNotFoundException("개설할 계좌의 사용자를 찾을 수 없습니다.");
    }
    req.put("ciHash", ciHash);
    MydataRiaAccountsResponseDTO response = restClient.post().uri(myDataUrl + "/api/mydata/ria-accounts")
        .contentType(MediaType.APPLICATION_JSON).body(req).retrieve()
        .body(MydataRiaAccountsResponseDTO.class);
    BigDecimal sumRiaLimit = Objects.requireNonNull(response, "myData 호출에 실패했습니다.")
        .getData().stream()
        .filter(data->!data.getBrokerName().equals(ownBrokerName))
        .map(MydataRiaAccountsResponseDTO.MyDataAccountResponse::getRiaLimit)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return MAX_LIMIT_AMOUNT.subtract(sumRiaLimit);
  }

  private Boolean addAccountToMydata(AccountDTO account) {
    Map<String, Object> req = new HashMap<>();

    String ciHash = accountMapper.selectCiHashByCustomerId(account.getCustomerId());
    if(ciHash.isBlank()){
      // TODO customer 도메인 제작 완료 후 수정
      // 추후 CustomerNotFoundException으로 변경 예정
      throw new AccountNotFoundException("개설할 계좌의 사용자를 찾을 수 없습니다.");
    }
    req.put("ciHash", ciHash);
    req.put("brokerName", ownBrokerName);
    req.put("riaLimit", account.getLimitAmount());
    req.put("riaCumlativeSell", 0);
    ResponseEntity<?> resEntity = restClient.post().uri(myDataUrl + "/api/mydata/ria-accounts/save")
        .contentType(MediaType.APPLICATION_JSON).body(req).retrieve()
        .toEntity(Object.class);

    return resEntity.getStatusCode().is2xxSuccessful();
  }

  Optional<AutomaticRejectionReason> findAutomaticRejectionReason(Long customerId) {
    // TODO 자동 반려 조건 1
    // 고객 확인 API의 residencyStatus가 UNKNOWN 또는 UNVERIFIED인지 확인
    // NON_RESIDENT가 확정된 경우에는 자동 반려가 아니라 가입 자격 예외로 차단
    boolean residencyVerified = true;

    // TODO 자동 반려 조건 2
    // 고객 확인 API의 CI와 customer에 저장된 ci_hash를 같은 해시 규칙으로 비교
    // 사용자 요청 DTO의 이름이나 생년월일을 신뢰해서 비교하지 않음
    boolean identityDataMatched = true;

    // TODO 자동 반려 조건 3
    // 타 금융회사 한도 응답의 customerKey가 고객 CI와 일치하고,
    // 기관별 limitAmount 합계가 응답의 totalLimitAmount와 같은지 확인
    // API timeout이나 서버 오류는 false가 아니라 처리 예외로 반환
    boolean externalLimitDataMatched = true;

    return determineAutomaticRejectionReason(residencyVerified, identityDataMatched, externalLimitDataMatched);
  }

  static Optional<AutomaticRejectionReason> determineAutomaticRejectionReason( boolean residencyVerified, boolean identityDataMatched, boolean externalLimitDataMatched) {
    if (!residencyVerified) {
      return Optional.of(AutomaticRejectionReason.RESIDENCY_UNVERIFIED);
    }
    if (!identityDataMatched) {
      return Optional.of(AutomaticRejectionReason.IDENTITY_DATA_MISMATCH);
    }
    if (!externalLimitDataMatched) {
      return Optional.of(AutomaticRejectionReason.EXTERNAL_LIMIT_DATA_MISMATCH);
    }
    return Optional.empty();
  }

  private void validateLimitAvailability(BigDecimal requestedLimit, BigDecimal availableLimit) {
    if (availableLimit.compareTo(MIN_LIMIT_AMOUNT) < 0) {
      throw new InvalidAccountRequestException("설정 가능한 RIA 납입한도가 없어 계좌를 개설할 수 없습니다.");
    }
    if (requestedLimit.compareTo(availableLimit) > 0) {
      throw new InvalidAccountRequestException(
          "계좌의 한도는 " + MIN_LIMIT_AMOUNT + "부터 " + availableLimit + "이하 입니다."
      );
    }
  }

  private void validateLimitInput(BigDecimal requestedLimit) {
    if (requestedLimit == null) {
      throw new InvalidAccountRequestException("계좌 한도 입력이 필요합니다.");
    }
    if (requestedLimit.compareTo(MIN_LIMIT_AMOUNT) < 0) {
      throw new InvalidAccountRequestException("계좌의 한도는 " + MIN_LIMIT_AMOUNT + "원 이상이어야 합니다.");
    }
    if (requestedLimit.stripTrailingZeros().scale() > 0) {
      throw new InvalidAccountRequestException("계좌의 한도는 원 단위로 입력해야 합니다.");
    }
    if (requestedLimit.compareTo(MAX_LIMIT_AMOUNT) > 0) {
      throw new InvalidAccountRequestException("계좌의 한도는 " + MAX_LIMIT_AMOUNT + "원 이하여야 합니다.");
    }
  }

  private void validateCustomerExists(Long customerId) {
    if (!accountMapper.existsCustomerById(customerId)) {
      // TODO customer 도메인 제작 완료 후 수정
      // 추후 CustomerNotFoundException으로 변경 예정
      throw new AccountNotFoundException("개설할 계좌의 사용자를 찾을 수 없습니다.");
    }
  }

  private LocalDateTime getApplicationTime() {
    LocalDateTime applicationTime = businessClockService.now();
    LocalDate today = applicationTime.toLocalDate();
    if (today.isBefore(RIA_APPLICATION_START_DATE) || today.isAfter(RIA_APPLICATION_END_DATE)) {
      throw new InvalidAccountRequestException("RIA 계좌 신청 가능 기간이 아닙니다.");
    }
    return applicationTime;
  }

  private void openAccountWithRetry(AccountDTO account) {
    boolean override = account.getStatus() == Status.REJECTED;

    for (int attempt = 1; attempt <= ACCOUNT_NO_RETRY_LIMIT; attempt++) {
      long accountNo = ThreadLocalRandom.current().nextLong(ACCOUNT_NO_MIN, ACCOUNT_NO_MAX_EXCLUSIVE);
      account.setAccountNo(Long.toString(accountNo));

      try {
        int updatedRows = override ? accountMapper.overrideToOpened(account) : accountMapper.approve(account);
        if (updatedRows < 1) {
          String message = override ? "계좌 오버라이드 실패" : "사용자 계좌 신청 승인 실패";
          throw new InvalidAccountRequestException(message);
        }
        return;
      } catch (DuplicateKeyException e) {
        if (attempt == ACCOUNT_NO_RETRY_LIMIT) {
          throw new AccountException("고유한 계좌번호 생성에 실패했습니다.");
        }
      }
    }
  }

  private String normalizeReason(String reason) {
    return reason.trim();
  }

  private String buildLimitChangeReason(BigDecimal previousLimit, BigDecimal newLimit) {
    return LIMIT_CHANGE_REASON_PREFIX
        + previousLimit.stripTrailingZeros().toPlainString()
        + "|to="
        + newLimit.stripTrailingZeros().toPlainString();
  }
}
