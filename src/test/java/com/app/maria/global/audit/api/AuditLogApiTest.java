package com.app.maria.global.audit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;
import com.app.maria.global.audit.service.AuditLogService;
import com.app.maria.global.config.SecurityConfig;
import com.app.maria.global.jwt.JwtTokenProvider;
import com.app.maria.global.response.PageResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditLogApi.class)
@Import(SecurityConfig.class)
class AuditLogApiTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuditLogService auditLogService;

    @MockitoBean JwtTokenProvider jwtTokenProvider;

    private AuditLogResponseDTO auditLog(Long auditId) {
        return AuditLogResponseDTO.builder()
                .auditId(auditId)
                .adminId(1L)
                .targetTable("ADMIN_USER")
                .targetPk("2")
                .beforeValue("VIEWER")
                .afterValue("ADMIN")
                .reasonCode("ADMIN_ROLE_UPDATE")
                .processedAt(LocalDateTime.of(2026, 8, 10, 10, 0))
                .build();
    }

    @Test
    @DisplayName("필터 없이 조회하면 200과 페이지 응답을 반환한다")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns200WithPageResponseWhenNoFilters() throws Exception {
        when(auditLogService.searchAuditLogs(any()))
                .thenReturn(PageResponseDTO.of(List.of(auditLog(1L), auditLog(2L)), 2L, 0, 20));

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("감사로그 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].auditId").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("VIEWER 역할도 조회할 수 있다 (VIEWER 이상 전부 허용 정책)")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns200ForViewerRole() throws Exception {
        when(auditLogService.searchAuditLogs(any()))
                .thenReturn(PageResponseDTO.of(List.of(), 0L, 0, 20));

        mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("검색조건 쿼리파라미터가 Request DTO에 바인딩되어 Service로 전달된다")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogsBindsQueryParamsIntoRequestDto() throws Exception {
        when(auditLogService.searchAuditLogs(any()))
                .thenReturn(PageResponseDTO.of(List.of(), 0L, 0, 20));

        mockMvc.perform(
                        get("/api/admin/audit-logs")
                                .param("targetTable", "ADMIN_USER")
                                .param("adminKeyword", "박지훈")
                                .param("targetKeyword", "이국희")
                                .param("reasonKeyword", "권한")
                                .param("startDate", "2026-01-01T00:00:00")
                                .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk());

        ArgumentCaptor<AuditLogSearchRequestDTO> captor =
                ArgumentCaptor.forClass(AuditLogSearchRequestDTO.class);
        verify(auditLogService).searchAuditLogs(captor.capture());
        AuditLogSearchRequestDTO bound = captor.getValue();
        assertThat(bound.getTargetTable()).isEqualTo("ADMIN_USER");
        assertThat(bound.getAdminKeyword()).isEqualTo("박지훈");
        assertThat(bound.getTargetKeyword()).isEqualTo("이국희");
        assertThat(bound.getReasonKeyword()).isEqualTo("권한");
        assertThat(bound.getStartDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(bound.getEndDate()).isEqualTo(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
    }

    @Test
    @DisplayName("page/size 쿼리파라미터가 Request DTO에 바인딩된다")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogsBindsPageAndSizeQueryParams() throws Exception {
        when(auditLogService.searchAuditLogs(any()))
                .thenReturn(PageResponseDTO.of(List.of(), 0L, 1, 5));

        mockMvc.perform(get("/api/admin/audit-logs").param("page", "1").param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<AuditLogSearchRequestDTO> captor =
                ArgumentCaptor.forClass(AuditLogSearchRequestDTO.class);
        verify(auditLogService).searchAuditLogs(captor.capture());
        assertThat(captor.getValue().getPage()).isEqualTo(1);
        assertThat(captor.getValue().getSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("page/size를 생략하면 기본값(0, 20)이 바인딩된다")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsUsesDefaultPageAndSizeWhenOmitted() throws Exception {
        when(auditLogService.searchAuditLogs(any()))
                .thenReturn(PageResponseDTO.of(List.of(), 0L, 0, 20));

        mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk());

        ArgumentCaptor<AuditLogSearchRequestDTO> captor =
                ArgumentCaptor.forClass(AuditLogSearchRequestDTO.class);
        verify(auditLogService).searchAuditLogs(captor.capture());
        assertThat(captor.getValue().getPage()).isEqualTo(0);
        assertThat(captor.getValue().getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("size가 100을 초과하면 400을 반환한다")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns400WhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(auditLogService, never()).searchAuditLogs(any());
    }

    @Test
    @DisplayName("page가 음수면 400을 반환한다")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns400WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verify(auditLogService, never()).searchAuditLogs(any());
    }

    @Test
    @DisplayName("page가 상한을 초과하면 400을 반환한다 (page*size 오버플로 방지)")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns400WhenPageExceedsMax() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs").param("page", "1000001"))
                .andExpect(status().isBadRequest());

        verify(auditLogService, never()).searchAuditLogs(any());
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 400을 반환하고 서비스는 호출되지 않는다")
    @WithMockUser(roles = "VIEWER")
    void searchAuditLogsReturns400WhenStartDateAfterEndDate() throws Exception {
        mockMvc.perform(
                        get("/api/admin/audit-logs")
                                .param("startDate", "2026-08-10T00:00:00")
                                .param("endDate", "2026-08-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("시작일은 종료일보다 늦을 수 없습니다."));

        verify(auditLogService, never()).searchAuditLogs(any());
    }

    @Test
    @DisplayName("인증되지 않은 요청이면 401을 반환하고 서비스는 호출되지 않는다")
    void searchAuditLogsReturns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isUnauthorized());

        verify(auditLogService, never()).searchAuditLogs(any());
    }
}
