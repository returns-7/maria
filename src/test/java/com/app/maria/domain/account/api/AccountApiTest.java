package com.app.maria.domain.account.api;

import com.app.maria.domain.account.dto.request.AccountReapplyRequestDTO;
import com.app.maria.domain.account.dto.request.AccountRequestDTO;
import com.app.maria.domain.account.dto.response.AccountResponseDTO;
import com.app.maria.domain.account.service.AccountService;
import com.app.maria.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountApiTest {

  private AccountService accountService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    accountService = mock(AccountService.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new AccountApi(accountService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

//  @Test
//  void accountApiUsesAdminRolePolicy() throws Exception {
//    assertThat(AccountApi.class.getAnnotation(PreAuthorize.class).value())
//        .isEqualTo("hasAnyRole('ADMIN', 'SETTLEMENT', 'REVIEWER', 'VIEWER')");
//
//    assertThat(AccountApi.class.getDeclaredMethod("approve", Long.class)
//        .getAnnotation(PreAuthorize.class).value())
//        .isEqualTo("hasAnyRole('ADMIN', 'REVIEWER')");
//    assertThat(AccountApi.class.getDeclaredMethod("reject", Long.class, ReasonRequestDTO.class)
//        .getAnnotation(PreAuthorize.class).value())
//        .isEqualTo("hasAnyRole('ADMIN', 'REVIEWER')");
//    assertThat(AccountApi.class.getDeclaredMethod("override", Long.class, ReasonRequestDTO.class)
//        .getAnnotation(PreAuthorize.class).value())
//        .isEqualTo("hasAnyRole('ADMIN', 'REVIEWER')");
//
//    assertThat(AccountApi.class.getDeclaredMethod("apply", AccountRequestDTO.class)
//        .getAnnotation(PreAuthorize.class))
//        .isNull();
//    assertThat(AccountApi.class.getDeclaredMethod("reapply", Long.class, AccountReapplyRequestDTO.class)
//        .getAnnotation(PreAuthorize.class))
//        .isNull();
//    assertThat(AccountApi.class.getDeclaredMethod("getAccount", Long.class)
//        .getAnnotation(PreAuthorize.class))
//        .isNull();
//  }

  @Test
  void applyAcceptsValidRequest() throws Exception {
    AccountResponseDTO response = mock(AccountResponseDTO.class);
    when(accountService.applyAccount(any(AccountRequestDTO.class))).thenReturn(response);

    mockMvc.perform(post("/api/account/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": 1,
                  "limitAmount": 30000000
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("계좌 개설 신청 처리 완료"));

    verify(accountService).applyAccount(any(AccountRequestDTO.class));
  }

  @Test
  void applyRejectsMissingCustomerId() throws Exception {
    mockMvc.perform(post("/api/account/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "limitAmount": 30000000
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("개설할 계좌의 사용자 정보는 필수입니다."));

    verify(accountService, never()).applyAccount(any());
  }

  @Test
  void applyRejectsNonPositiveCustomerId() throws Exception {
    mockMvc.perform(post("/api/account/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": 0,
                  "limitAmount": 30000000
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사용자 ID는 0보다 커야 합니다."));

    verify(accountService, never()).applyAccount(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "50000001", "1.5"})
  void applyRejectsInvalidLimit(String limitAmount) throws Exception {
    mockMvc.perform(post("/api/account/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": 1,
                  "limitAmount": %s
                }
                """.formatted(limitAmount)))
        .andExpect(status().isBadRequest());

    verify(accountService, never()).applyAccount(any());
  }

  @Test
  void applyRejectsMissingLimit() throws Exception {
    mockMvc.perform(post("/api/account/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "customerId": 1
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("계좌 한도는 필수입니다."));

    verify(accountService, never()).applyAccount(any());
  }

  @Test
  void getAvailableLimitRejectsNonPositiveCustomerId() throws Exception {
    mockMvc.perform(get("/api/account/available-limit")
            .param("customerId", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사용자 ID는 0보다 커야 합니다."));

    verify(accountService, never()).getAvailableLimit(any());
  }

  @Test
  void approveRejectsNonPositiveAccountId() throws Exception {
    mockMvc.perform(post("/api/account/0/approve"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("계좌 ID는 0보다 커야 합니다."));

    verify(accountService, never()).approveAccount(anyLong());
  }

  @Test
  void rejectRejectsBlankReason() throws Exception {
    mockMvc.perform(post("/api/account/1/reject")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": "   "
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사유를 입력해야 합니다."));

    verify(accountService, never()).rejectAccount(anyLong(), anyString());
  }

  @Test
  void rejectRejectsReasonLongerThanTwoHundredCharacters() throws Exception {
    String reason = "a".repeat(201);

    mockMvc.perform(post("/api/account/1/reject")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": "%s"
                }
                """.formatted(reason)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사유는 200자 이하로 입력해야 합니다."));

    verify(accountService, never()).rejectAccount(anyLong(), anyString());
  }

  @Test
  void reapplyAcceptsValidRequest() throws Exception {
    AccountResponseDTO response = mock(AccountResponseDTO.class);
    when(accountService.reapplyAccountByAccountId(anyLong(), any(AccountReapplyRequestDTO.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/account/1/reapply")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "limitAmount": 20000000
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("계좌 재신청"));

    verify(accountService).reapplyAccountByAccountId(anyLong(), any(AccountReapplyRequestDTO.class));
  }

  @Test
  void reapplyAcceptsMissingLimitForCurrentLimitReuse() throws Exception {
    AccountResponseDTO response = mock(AccountResponseDTO.class);
    when(accountService.reapplyAccountByAccountId(anyLong(), any(AccountReapplyRequestDTO.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/account/1/reapply")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("계좌 재신청"));

    verify(accountService).reapplyAccountByAccountId(anyLong(), any(AccountReapplyRequestDTO.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "50000001", "1.5"})
  void reapplyRejectsInvalidLimit(String limitAmount) throws Exception {
    mockMvc.perform(post("/api/account/1/reapply")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "limitAmount": %s
                }
                """.formatted(limitAmount)))
        .andExpect(status().isBadRequest());

    verify(accountService, never())
        .reapplyAccountByAccountId(anyLong(), any(AccountReapplyRequestDTO.class));
  }

  @Test
  void overrideRejectsBlankReason() throws Exception {
    mockMvc.perform(post("/api/account/1/override")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "reason": ""
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("사유를 입력해야 합니다."));

    verify(accountService, never()).overrideAccount(anyLong(), anyString());
  }
}
