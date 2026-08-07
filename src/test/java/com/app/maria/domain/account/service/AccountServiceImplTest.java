//package com.app.maria.domain.account.service;
//
//import com.app.maria.domain.account.dto.AccountDTO;
//import com.app.maria.domain.account.dto.AccountStatusLogDTO;
//import com.app.maria.domain.account.dto.request.AccountReapplyRequestDTO;
//import com.app.maria.domain.account.dto.request.AccountRequestDTO;
//import com.app.maria.domain.account.dto.response.AccountLogResponseDTO;
//import com.app.maria.domain.account.dto.response.AccountResponseDTO;
//import com.app.maria.domain.account.exception.AccountException;
//import com.app.maria.domain.account.exception.DuplicateAccountException;
//import com.app.maria.domain.account.exception.InvalidAccountRequestException;
//import com.app.maria.domain.account.mapper.AccountMapper;
//import com.app.maria.domain.account.mapper.AccountStatusLogMapper;
//import com.app.maria.domain.account.type.AutomaticRejectionReason;
//import com.app.maria.domain.account.type.Status;
//import com.app.maria.global.clock.service.BusinessClockService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.EnumSource;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.dao.DuplicateKeyException;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.lenient;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AccountServiceImplTest {
//
//  private static final Long CUSTOMER_ID = 1L;
//  private static final Long ACCOUNT_ID = 10L;
//  private static final BigDecimal LIMIT_AMOUNT = BigDecimal.valueOf(30_000_000L);
//  private static final BigDecimal MAX_LIMIT_AMOUNT = BigDecimal.valueOf(50_000_000L);
//  private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 8, 2, 10, 30);
//
//  @Mock
//  private AccountMapper accountMapper;
//
//  @Mock
//  private AccountStatusLogMapper accountStatusLogMapper;
//
//  @Mock
//  private BusinessClockService businessClockService;
//
//  @Spy
//  @InjectMocks
//  private AccountServiceImpl accountService;
//
//  @BeforeEach
//  void setUpBusinessClock() {
//    lenient().when(businessClockService.now()).thenReturn(FIXED_NOW);
//  }
//
//  @Test
//  @DisplayName("타 금융회사 한도가 없으면 설정 가능 최대 한도는 5천만원이다")
//  void getAvailableLimitReturnsFiftyMillion() {
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      BigDecimal result = accountService.getAvailableLimit(CUSTOMER_ID);
//
//      assertThat(result).isEqualByComparingTo(MAX_LIMIT_AMOUNT);
//    }
//  }
//
//  @Test
//  @DisplayName("정상 계좌 신청은 APPLIED 이력 생성 후 자동으로 OPENED 처리된다")
//  void applyAccountAutomaticallyOpensAccount() {
//    AccountDTO appliedAccount = account(Status.APPLIED);
//    AccountDTO openedAccount = account(Status.OPENED);
//
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);
//    when(accountMapper.insertApplication(any(AccountDTO.class))).thenReturn(1);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(appliedAccount));
//    when(accountMapper.approve(any(AccountDTO.class))).thenAnswer(invocation -> {
//      AccountDTO approvingAccount = invocation.getArgument(0, AccountDTO.class);
//      openedAccount.setAccountNo(approvingAccount.getAccountNo());
//      openedAccount.setOpenedAt(approvingAccount.getOpenedAt());
//      return 1;
//    });
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(openedAccount));
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.APPLIED, Status.OPENED, "자동 판정 승인")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.applyAccount(request(LIMIT_AMOUNT));
//
//      assertThat(result.getStatus()).isEqualTo(Status.OPENED);
//      assertThat(result.getOpenedAt()).isEqualTo(FIXED_NOW);
//      assertThat(result.getAccountNo()).matches("[0-9]{10}");
//    }
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper, Mockito.times(2)).insertLog(logCaptor.capture());
//
//    List<AccountStatusLogDTO> logs = logCaptor.getAllValues();
//    assertThat(logs.get(0).getPrevStatus()).isNull();
//    assertThat(logs.get(0).getNewStatus()).isEqualTo(Status.APPLIED);
//    assertThat(logs.get(0).getReason()).isEqualTo("최초 개설 신청");
//    assertThat(logs.get(1).getPrevStatus()).isEqualTo(Status.APPLIED);
//    assertThat(logs.get(1).getNewStatus()).isEqualTo(Status.OPENED);
//    assertThat(logs.get(1).getReason()).isEqualTo("자동 판정 승인");
//  }
//
//  @ParameterizedTest
//  @EnumSource(AutomaticRejectionReason.class)
//  @DisplayName("자동 판정 사유가 있으면 신청 계좌를 REJECTED 처리하고 이력을 저장한다")
//  void applyAccountAutomaticallyRejectsAccount(AutomaticRejectionReason rejectionReason) {
//    AccountDTO appliedAccount = account(Status.APPLIED);
//    AccountDTO rejectedAccount = account(Status.REJECTED);
//
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);
//    doReturn(Optional.of(rejectionReason))
//        .when(accountService)
//        .findAutomaticRejectionReason(CUSTOMER_ID);
//    when(accountMapper.insertApplication(any(AccountDTO.class))).thenReturn(1);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(appliedAccount));
//    when(accountMapper.reject(appliedAccount)).thenReturn(1);
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(rejectedAccount));
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(
//            Status.APPLIED,
//            Status.REJECTED,
//            rejectionReason.toLogReason()
//        )));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.applyAccount(request(LIMIT_AMOUNT));
//
//      assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
//      assertThat(result.getAccountNo()).isNull();
//      assertThat(result.getOpenedAt()).isNull();
//    }
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper, Mockito.times(2)).insertLog(logCaptor.capture());
//    assertThat(logCaptor.getAllValues().get(1).getPrevStatus()).isEqualTo(Status.APPLIED);
//    assertThat(logCaptor.getAllValues().get(1).getNewStatus()).isEqualTo(Status.REJECTED);
//    assertThat(logCaptor.getAllValues().get(1).getReason())
//        .isEqualTo(rejectionReason.toLogReason());
//    verify(accountMapper, never()).approve(any(AccountDTO.class));
//  }
//
//  @Test
//  @DisplayName("이미 계좌가 있는 고객의 신청은 중복 계좌 예외로 차단한다")
//  void applyAccountRejectsDuplicateCustomerAccount() {
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      assertThatThrownBy(() -> accountService.applyAccount(request(LIMIT_AMOUNT)))
//          .isInstanceOf(DuplicateAccountException.class)
//          .hasMessage("사용자의 기존 계좌 정보가 있습니다.");
//    }
//
//    verify(accountMapper, never()).insertApplication(any(AccountDTO.class));
//  }
//
//  @Test
//  @DisplayName("동시 신청으로 발생한 DB 중복 오류를 중복 계좌 예외로 변환한다")
//  void applyAccountConvertsDuplicateKeyException() {
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);
//    when(accountMapper.insertApplication(any(AccountDTO.class)))
//        .thenThrow(new DuplicateKeyException("uk_account_customer"));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      assertThatThrownBy(() -> accountService.applyAccount(request(LIMIT_AMOUNT)))
//          .isInstanceOf(DuplicateAccountException.class)
//          .hasMessage("사용자의 기존 계좌 정보가 있습니다.");
//    }
//  }
//
//  @ParameterizedTest
//  @EnumSource(value = Status.class, names = {"APPLIED", "OPENED"})
//  @DisplayName("APPLIED 또는 OPENED 계좌는 마이페이지에서 한도를 변경하고 동일 상태 이력을 저장한다")
//  void updateAccountLimitAllowsAppliedAndOpenedStatuses(Status status) {
//    BigDecimal changedLimit = BigDecimal.valueOf(40_000_000L);
//    AccountDTO currentAccount = account(status);
//    AccountDTO updatedAccount = account(status);
//    updatedAccount.setLimitAmount(changedLimit);
//
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(currentAccount));
//    when(accountMapper.updateLimit(ACCOUNT_ID, status, LIMIT_AMOUNT, changedLimit)).thenReturn(1);
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(updatedAccount));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.updateAccountLimit(
//          CUSTOMER_ID,
//          LIMIT_AMOUNT,
//          changedLimit
//      );
//
//      assertThat(result.getStatus()).isEqualTo(status);
//      assertThat(result.getLimitAmount()).isEqualByComparingTo(changedLimit);
//    }
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper).insertLog(logCaptor.capture());
//    AccountStatusLogDTO savedLog = logCaptor.getValue();
//    assertThat(savedLog.getPrevStatus()).isEqualTo(status);
//    assertThat(savedLog.getNewStatus()).isEqualTo(status);
//    assertThat(savedLog.getChangedAt()).isEqualTo(FIXED_NOW);
//    assertThat(savedLog.getReason()).isEqualTo(
//        "LIMIT_CHANGE|from=30000000|to=40000000"
//    );
//  }
//
//  @ParameterizedTest
//  @EnumSource(value = Status.class, names = {"REJECTED", "CLOSURE_REQUESTED", "CLOSED"})
//  @DisplayName("REJECTED, CLOSURE_REQUESTED, CLOSED 계좌는 마이페이지 한도 변경을 차단한다")
//  void updateAccountLimitRejectsDisallowedStatuses(Status status) {
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(account(status)));
//
//    assertThatThrownBy(() -> accountService.updateAccountLimit(
//        CUSTOMER_ID,
//        LIMIT_AMOUNT,
//        BigDecimal.valueOf(40_000_000L)
//    )).isInstanceOf(InvalidAccountRequestException.class)
//        .hasMessage("신청 또는 개설 상태의 계좌만 한도를 변경할 수 있습니다.");
//
//    verify(accountMapper, never()).updateLimit(any(), any(), any(), any());
//    verify(accountStatusLogMapper, never()).insertLog(any(AccountStatusLogDTO.class));
//  }
//
//  @Test
//  @DisplayName("화면이 조회한 기존 한도와 현재 DB 한도가 다르면 stale 한도 변경을 차단한다")
//  void updateAccountLimitRejectsStaleExpectedLimit() {
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID))
//        .thenReturn(Optional.of(account(Status.OPENED)));
//
//    assertThatThrownBy(() -> accountService.updateAccountLimit(
//        CUSTOMER_ID,
//        BigDecimal.valueOf(20_000_000L),
//        BigDecimal.valueOf(40_000_000L)
//    )).isInstanceOf(InvalidAccountRequestException.class)
//        .hasMessage("계좌 한도가 변경되었습니다. 다시 조회 후 시도해주세요.");
//
//    verify(accountMapper, never()).updateLimit(any(), any(), any(), any());
//  }
//
//  @Test
//  @DisplayName("조건부 한도 UPDATE가 실패하면 한도 변경 이력을 저장하지 않는다")
//  void updateAccountLimitDoesNotLogConditionalUpdateConflict() {
//    BigDecimal changedLimit = BigDecimal.valueOf(40_000_000L);
//    when(accountMapper.existsCustomerById(CUSTOMER_ID)).thenReturn(true);
//    when(accountMapper.selectByCustomerId(CUSTOMER_ID))
//        .thenReturn(Optional.of(account(Status.OPENED)));
//    when(accountMapper.updateLimit(ACCOUNT_ID, Status.OPENED, LIMIT_AMOUNT, changedLimit))
//        .thenReturn(0);
//
//    assertThatThrownBy(() -> accountService.updateAccountLimit(
//        CUSTOMER_ID,
//        LIMIT_AMOUNT,
//        changedLimit
//    )).isInstanceOf(InvalidAccountRequestException.class)
//        .hasMessage("계좌 한도 변경 중 상태 또는 한도가 변경되었습니다.");
//
//    verify(accountStatusLogMapper, never()).insertLog(any(AccountStatusLogDTO.class));
//  }
//
//  @Test
//  @DisplayName("계좌번호 unique 충돌이 발생하면 새 계좌번호로 승인 처리를 재시도한다")
//  void approveAccountRetriesAccountNumberCollision() {
//    AccountDTO appliedAccount = account(Status.APPLIED);
//    AccountDTO openedAccount = account(Status.OPENED);
//    List<String> generatedAccountNumbers = new ArrayList<>();
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(appliedAccount))
//        .thenReturn(Optional.of(openedAccount));
//    when(accountMapper.approve(any(AccountDTO.class))).thenAnswer(invocation -> {
//      AccountDTO approvingAccount = invocation.getArgument(0, AccountDTO.class);
//      generatedAccountNumbers.add(approvingAccount.getAccountNo());
//      if (generatedAccountNumbers.size() == 1) {
//        throw new DuplicateKeyException("uk_account_no");
//      }
//      openedAccount.setAccountNo(approvingAccount.getAccountNo());
//      openedAccount.setOpenedAt(approvingAccount.getOpenedAt());
//      return 1;
//    });
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.APPLIED, Status.OPENED, "사용자 계좌 개설")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.approveAccount(ACCOUNT_ID);
//
//      assertThat(result.getStatus()).isEqualTo(Status.OPENED);
//      assertThat(generatedAccountNumbers).hasSize(2);
//      assertThat(generatedAccountNumbers).allSatisfy(accountNo ->
//          assertThat(accountNo).matches("[0-9]{10}")
//      );
//    }
//  }
//
//  @Test
//  @DisplayName("계좌번호 충돌이 5회 발생하면 내부 처리 예외를 반환한다")
//  void approveAccountFailsAfterFiveAccountNumberCollisions() {
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account(Status.APPLIED)));
//    when(accountMapper.approve(any(AccountDTO.class)))
//        .thenThrow(new DuplicateKeyException("uk_account_no"));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      assertThatThrownBy(() -> accountService.approveAccount(ACCOUNT_ID))
//          .isInstanceOf(AccountException.class)
//          .hasMessage("고유한 계좌번호 생성에 실패했습니다.");
//    }
//
//    verify(accountMapper, Mockito.times(5)).approve(any(AccountDTO.class));
//    verify(accountStatusLogMapper, never()).insertLog(any(AccountStatusLogDTO.class));
//  }
//
//  @Test
//  @DisplayName("관리자가 입력한 반려 사유를 정리해 REJECTED 상태 이력에 저장한다")
//  void rejectAccountRecordsReason() {
//    AccountDTO appliedAccount = account(Status.APPLIED);
//    AccountDTO rejectedAccount = account(Status.REJECTED);
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(appliedAccount))
//        .thenReturn(Optional.of(rejectedAccount));
//    when(accountMapper.reject(appliedAccount)).thenReturn(1);
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.APPLIED, Status.REJECTED, "서류 확인 필요")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.rejectAccount(ACCOUNT_ID, "  서류 확인 필요  ");
//
//      assertThat(result.getStatus()).isEqualTo(Status.REJECTED);
//    }
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper).insertLog(logCaptor.capture());
//    assertThat(logCaptor.getValue().getPrevStatus()).isEqualTo(Status.APPLIED);
//    assertThat(logCaptor.getValue().getNewStatus()).isEqualTo(Status.REJECTED);
//    assertThat(logCaptor.getValue().getReason()).isEqualTo("서류 확인 필요");
//    assertThat(logCaptor.getValue().getChangedAt()).isEqualTo(FIXED_NOW);
//  }
//
//  @Test
//  @DisplayName("반려 계좌는 관리자 사유와 함께 OPENED 상태로 오버라이드할 수 있다")
//  void overrideRejectedAccountOpensAccount() {
//    AccountDTO rejectedAccount = account(Status.REJECTED);
//    AccountDTO openedAccount = account(Status.OPENED);
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(rejectedAccount))
//        .thenReturn(Optional.of(openedAccount));
//    when(accountMapper.overrideToOpened(any(AccountDTO.class))).thenAnswer(invocation -> {
//      AccountDTO overridingAccount = invocation.getArgument(0, AccountDTO.class);
//      openedAccount.setAccountNo(overridingAccount.getAccountNo());
//      openedAccount.setOpenedAt(overridingAccount.getOpenedAt());
//      return 1;
//    });
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.REJECTED, Status.OPENED, "관리자 확인 완료")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.overrideAccount(ACCOUNT_ID, " 관리자 확인 완료 ");
//
//      assertThat(result.getStatus()).isEqualTo(Status.OPENED);
//      assertThat(result.getOpenedAt()).isEqualTo(FIXED_NOW);
//      assertThat(result.getAccountNo()).isNotNull();
//    }
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper).insertLog(logCaptor.capture());
//    assertThat(logCaptor.getValue().getPrevStatus()).isEqualTo(Status.REJECTED);
//    assertThat(logCaptor.getValue().getNewStatus()).isEqualTo(Status.OPENED);
//    assertThat(logCaptor.getValue().getReason()).isEqualTo("관리자 확인 완료");
//  }
//
//  @Test
//  @DisplayName("반려 상태가 아닌 계좌는 관리자 오버라이드를 차단한다")
//  void overrideAccountRejectsNonRejectedStatus() {
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account(Status.OPENED)));
//
//    assertThatThrownBy(() -> accountService.overrideAccount(ACCOUNT_ID, "관리자 확인 완료"))
//        .isInstanceOf(InvalidAccountRequestException.class)
//        .hasMessage("반려 상태의 계좌만 오버라이드할 수 있습니다.");
//
//    verify(accountMapper, never()).overrideToOpened(any(AccountDTO.class));
//  }
//
//  @Test
//  @DisplayName("반려 계좌를 새 한도로 재신청하면 APPLIED 상태와 이력을 저장한다")
//  void reapplyRejectedAccountChangesLimitAndStatus() {
//    AccountDTO rejectedAccount = account(Status.REJECTED);
//    AccountDTO reappliedAccount = account(Status.APPLIED);
//    BigDecimal changedLimit = BigDecimal.valueOf(20_000_000L);
//    reappliedAccount.setLimitAmount(changedLimit);
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(rejectedAccount))
//        .thenReturn(Optional.of(reappliedAccount));
//    when(accountMapper.reapply(any(AccountDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.REJECTED, Status.APPLIED, "사용자 계좌 개설 재신청")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.reapplyAccountByAccountId(
//          ACCOUNT_ID,
//          reapplyRequest(changedLimit)
//      );
//
//      assertThat(result.getStatus()).isEqualTo(Status.APPLIED);
//      assertThat(result.getLimitAmount()).isEqualByComparingTo(changedLimit);
//    }
//
//    ArgumentCaptor<AccountDTO> accountCaptor = ArgumentCaptor.forClass(AccountDTO.class);
//    verify(accountMapper).reapply(accountCaptor.capture());
//    assertThat(accountCaptor.getValue().getAccountId()).isEqualTo(ACCOUNT_ID);
//    assertThat(accountCaptor.getValue().getLimitAmount()).isEqualByComparingTo(changedLimit);
//
//    ArgumentCaptor<AccountStatusLogDTO> logCaptor = ArgumentCaptor.forClass(AccountStatusLogDTO.class);
//    verify(accountStatusLogMapper).insertLog(logCaptor.capture());
//    assertThat(logCaptor.getValue().getPrevStatus()).isEqualTo(Status.REJECTED);
//    assertThat(logCaptor.getValue().getNewStatus()).isEqualTo(Status.APPLIED);
//  }
//
//  @Test
//  @DisplayName("재신청 한도를 생략하면 기존 계좌 한도를 유지한다")
//  void reapplyRejectedAccountKeepsCurrentLimitWhenOmitted() {
//    AccountDTO rejectedAccount = account(Status.REJECTED);
//    AccountDTO reappliedAccount = account(Status.APPLIED);
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(rejectedAccount))
//        .thenReturn(Optional.of(reappliedAccount));
//    when(accountMapper.reapply(any(AccountDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.insertLog(any(AccountStatusLogDTO.class))).thenReturn(1);
//    when(accountStatusLogMapper.selectLatestByAccountId(ACCOUNT_ID))
//        .thenReturn(Optional.of(statusLog(Status.REJECTED, Status.APPLIED, "사용자 계좌 개설 재신청")));
//
//    try (MockedStatic<LocalDateTime> ignored = mockCurrentDateTime()) {
//      AccountResponseDTO result = accountService.reapplyAccountByAccountId(
//          ACCOUNT_ID,
//          reapplyRequest(null)
//      );
//
//      assertThat(result.getLimitAmount()).isEqualByComparingTo(LIMIT_AMOUNT);
//    }
//
//    ArgumentCaptor<AccountDTO> accountCaptor = ArgumentCaptor.forClass(AccountDTO.class);
//    verify(accountMapper).reapply(accountCaptor.capture());
//    assertThat(accountCaptor.getValue().getLimitAmount()).isEqualByComparingTo(LIMIT_AMOUNT);
//  }
//
//  @Test
//  @DisplayName("계좌 상태 이력을 오래된 순서대로 응답 DTO로 반환한다")
//  void getStatusLogsReturnsMappedHistory() {
//    List<AccountStatusLogDTO> logs = List.of(
//        statusLog(null, Status.APPLIED, "최초 개설 신청"),
//        statusLog(Status.APPLIED, Status.OPENED, "자동 판정 승인")
//    );
//
//    when(accountMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account(Status.OPENED)));
//    when(accountStatusLogMapper.selectByAccountId(ACCOUNT_ID)).thenReturn(logs);
//
//    List<AccountLogResponseDTO> result = accountService.getStatusLogsByAccountId(ACCOUNT_ID);
//
//    assertThat(result).hasSize(2);
//    assertThat(result.get(0).getPrevStatus()).isNull();
//    assertThat(result.get(0).getNewStatus()).isEqualTo(Status.APPLIED);
//    assertThat(result.get(1).getPrevStatus()).isEqualTo(Status.APPLIED);
//    assertThat(result.get(1).getNewStatus()).isEqualTo(Status.OPENED);
//  }
//
//  @Test
//  @DisplayName("모든 자동 심사 자료가 정상이면 반려 사유가 없다")
//  void determineAutomaticRejectionReasonReturnsEmptyWhenAllChecksPass() {
//    Optional<AutomaticRejectionReason> result =
//        AccountServiceImpl.determineAutomaticRejectionReason(true, true, true);
//
//    assertThat(result).isEmpty();
//  }
//
//  @ParameterizedTest
//  @MethodSource("automaticRejectionCases")
//  @DisplayName("자동 심사 자료가 불일치하면 우선순위에 맞는 반려 사유를 반환한다")
//  void determineAutomaticRejectionReasonReturnsExpectedReason(
//      boolean residencyVerified,
//      boolean identityDataMatched,
//      boolean externalLimitDataMatched,
//      AutomaticRejectionReason expectedReason
//  ) {
//    Optional<AutomaticRejectionReason> result =
//        AccountServiceImpl.determineAutomaticRejectionReason(
//            residencyVerified,
//            identityDataMatched,
//            externalLimitDataMatched
//        );
//
//    assertThat(result).contains(expectedReason);
//  }
//
//  private static Stream<Arguments> automaticRejectionCases() {
//    return Stream.of(
//        Arguments.of(
//            false,
//            true,
//            true,
//            AutomaticRejectionReason.RESIDENCY_UNVERIFIED
//        ),
//        Arguments.of(
//            true,
//            false,
//            true,
//            AutomaticRejectionReason.IDENTITY_DATA_MISMATCH
//        ),
//        Arguments.of(
//            true,
//            true,
//            false,
//            AutomaticRejectionReason.EXTERNAL_LIMIT_DATA_MISMATCH
//        )
//    );
//  }
//
//  private MockedStatic<LocalDateTime> mockCurrentDateTime() {
//    MockedStatic<LocalDateTime> mockedDateTime = Mockito.mockStatic(LocalDateTime.class);
//    mockedDateTime.when(LocalDateTime::now).thenReturn(FIXED_NOW);
//    return mockedDateTime;
//  }
//
//  private AccountRequestDTO request(BigDecimal limitAmount) {
//    return AccountRequestDTO.builder()
//        .customerId(CUSTOMER_ID)
//        .limitAmount(limitAmount)
//        .build();
//  }
//
//  private AccountReapplyRequestDTO reapplyRequest(BigDecimal limitAmount) {
//    return AccountReapplyRequestDTO.builder()
//        .limitAmount(limitAmount)
//        .build();
//  }
//
//  private AccountDTO account(Status status) {
//    return AccountDTO.builder()
//        .accountId(ACCOUNT_ID)
//        .customerId(CUSTOMER_ID)
//        .status(status)
//        .createdAt(FIXED_NOW)
//        .limitAmount(LIMIT_AMOUNT)
//        .amount(BigDecimal.ZERO)
//        .build();
//  }
//
//  private AccountStatusLogDTO statusLog(Status prevStatus, Status newStatus, String reason) {
//    return AccountStatusLogDTO.builder()
//        .logId(1L)
//        .accountId(ACCOUNT_ID)
//        .prevStatus(prevStatus)
//        .newStatus(newStatus)
//        .changedAt(FIXED_NOW)
//        .reason(reason)
//        .build();
//  }
//}
