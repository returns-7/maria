package com.app.maria.domain.account.mapper;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.type.Status;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Mapper
public interface AccountMapper {
  //고객 존재 여부 확인
  boolean existsCustomerById(Long customerId);

  //account_id 기준 단건 조회
  Optional<AccountDTO> selectByAccountId(Long accountId);

  //account_id 기준 단건 조회 + 잠금
  Optional<AccountDTO> selectByAccountIdForUpdate(Long accountId);

  //customer_id 기준 단건 조회
  Optional<AccountDTO> selectByCustomerId(Long customerId);

  //customer_id 기준 계좌 존재 여부
  boolean existsByCustomerId(Long customerId);

  //최초 계좌 개설 신청
  int insertApplication(AccountDTO accountDTO);

  //재신청
  int reapply(AccountDTO accountDTO);

  //APPLIED 또는 OPENED 계좌의 설정한도 변경
  int updateLimit(Long accountId, Status status, BigDecimal expectedCurrentLimit, BigDecimal newLimitAmount);

  //신청 승인
  // - 계좌번호는 승인 시점에 최초 1회 발급
  // - 수정 건수가 0이면 이미 처리됐거나 신청 상태가 아님
  int approve(AccountDTO accountDTO);

  //신청 반려
  // - 반려 사유는 account_status_log에 별도로 저장
  // - 수정 건수가 0이면 이미 처리됐거나 신청 상태가 아님
  int reject(AccountDTO  accountDTO);

  //상태별 계좌 목록
  List<AccountDTO> selectAllAccount();

  //관리자 반려 판정 오버라이드
  int overrideToOpened(AccountDTO accountDTO);

  // customerId를 통해 ci_hash 값 가져오기
  String selectCiHashByCustomerId(Long customerId);
}
