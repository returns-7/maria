package com.app.maria.domain.account.service;

import com.app.maria.domain.account.dto.request.AccountRequestDTO;
import com.app.maria.domain.account.dto.request.AccountReapplyRequestDTO;
import com.app.maria.domain.account.dto.response.AccountLogResponseDTO;
import com.app.maria.domain.account.dto.response.AccountResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
  // 관리자 목록 조회
  List<AccountResponseDTO> findAll();

  // 고객의 전 금융회사 RIA 납입한도를 반영한 현재 설정 가능 최대 한도
  BigDecimal getAvailableLimit(Long customerId);

  // 고객 마이페이지용 한도 변경: APPLIED, OPENED 상태에서만 허용
  AccountResponseDTO updateAccountLimit(AccountRequestDTO accountRequestDTO);

  // 관리자 대리 신청과 사용자 본인 신청이 공통으로 사용
  AccountResponseDTO applyAccount(AccountRequestDTO requestDTO);

  // 관리자 기능
  AccountResponseDTO approveAccount(Long accountId);

  AccountResponseDTO rejectAccount(Long accountId, String reason);

  AccountResponseDTO reapplyAccountByAccountId(Long accountId, AccountReapplyRequestDTO requestDTO);

  AccountResponseDTO getAccountByAccountId(Long accountId);

  List<AccountLogResponseDTO> getStatusLogsByAccountId(Long accountId);

  AccountResponseDTO overrideAccount(Long accountId, String reason);
}
