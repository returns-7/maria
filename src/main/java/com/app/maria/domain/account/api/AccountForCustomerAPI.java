package com.app.maria.domain.account.api;

import com.app.maria.domain.account.dto.request.AccountRequestDTO;
import com.app.maria.domain.account.dto.response.AccountResponseDTO;
import com.app.maria.domain.account.service.AccountService;
import com.app.maria.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/account")
public class AccountForCustomerAPI {
  private final AccountService accountService;

  @PutMapping("/update/limit")
  @PreAuthorize("hasAnyRole('ADMIN', 'SETTLEMENT', 'REVIEWER', 'VIEWER')")
  public ResponseEntity<ApiResponseDTO<AccountResponseDTO>> updateLimit(@Valid @RequestBody AccountRequestDTO accountRequestDTO){
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.of("계좌 한도 변경", accountService.updateAccountLimit(accountRequestDTO)));
  }
}
