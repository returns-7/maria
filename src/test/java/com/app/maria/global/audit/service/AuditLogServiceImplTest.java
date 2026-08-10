package com.app.maria.global.audit.service;

import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.dto.AuditLogSearchDTO;
import com.app.maria.global.audit.dto.request.AuditLogSearchRequestDTO;
import com.app.maria.global.audit.dto.response.AuditLogResponseDTO;
import com.app.maria.global.audit.exception.AuditLogInsertException;
import com.app.maria.global.audit.mapper.AuditLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    AuditLogMapper auditLogMapper;

    @InjectMocks
    AuditLogServiceImpl auditLogService;

    private AuditLogDTO auditLog(Long auditId, Long adminId, String targetTable, String targetPk) {
        return AuditLogDTO.builder()
                .auditId(auditId)
                .adminId(adminId)
                .targetTable(targetTable)
                .targetPk(targetPk)
                .beforeValue("VIEWER")
                .afterValue("ADMIN")
                .reasonCode("ADMIN_ROLE_UPDATE")
                .processedAt(LocalDateTime.of(2026, 8, 10, 10, 0))
                .build();
    }

    @Test
    @DisplayName("검색 조건을 VO로 변환해 Mapper에 넘기고, 결과를 ResponseDTO 리스트로 변환해 반환한다")
    void searchAuditLogsConvertsRequestToVoAndMapsResultToResponseDto() {
        AuditLogSearchRequestDTO request = AuditLogSearchRequestDTO.builder()
                .adminId(1L)
                .targetTable("ADMIN_USER")
                .build();

        when(auditLogMapper.selectAuditLogs(any(AuditLogSearchDTO.class)))
                .thenReturn(List.of(auditLog(10L, 1L, "ADMIN_USER", "2")));

        List<AuditLogResponseDTO> result = auditLogService.searchAuditLogs(request);

        assertThat(result).hasSize(1);
        AuditLogResponseDTO response = result.get(0);
        assertThat(response.getAuditId()).isEqualTo(10L);
        assertThat(response.getAdminId()).isEqualTo(1L);
        assertThat(response.getTargetTable()).isEqualTo("ADMIN_USER");
        assertThat(response.getTargetPk()).isEqualTo("2");

        ArgumentCaptor<AuditLogSearchDTO> captor = ArgumentCaptor.forClass(AuditLogSearchDTO.class);
        verify(auditLogMapper).selectAuditLogs(captor.capture());
        AuditLogSearchDTO passedVo = captor.getValue();
        assertThat(passedVo.getAdminId()).isEqualTo(1L);
        assertThat(passedVo.getTargetTable()).isEqualTo("ADMIN_USER");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
    void searchAuditLogsReturnsEmptyListWhenNoResults() {
        AuditLogSearchRequestDTO request = AuditLogSearchRequestDTO.builder().build();
        when(auditLogMapper.selectAuditLogs(any(AuditLogSearchDTO.class))).thenReturn(List.of());

        List<AuditLogResponseDTO> result = auditLogService.searchAuditLogs(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("insert가 1건 성공하면 예외 없이 정상 종료한다")
    void logInsertsSuccessfullyWhenOneRowAffected() {
        AuditLogDTO auditLog = auditLog(null, 1L, "ADMIN_USER", "2");
        when(auditLogMapper.insertLog(auditLog)).thenReturn(1);

        auditLogService.log(auditLog);

        verify(auditLogMapper).insertLog(auditLog);
    }

    @Test
    @DisplayName("insert된 행이 1건이 아니면 AuditLogInsertException을 던진다")
    void logThrowsAuditLogInsertExceptionWhenInsertedRowsIsNotOne() {
        AuditLogDTO auditLog = auditLog(null, 1L, "ADMIN_USER", "2");
        when(auditLogMapper.insertLog(auditLog)).thenReturn(0);

        assertThatThrownBy(() -> auditLogService.log(auditLog))
                .isInstanceOf(AuditLogInsertException.class)
                .hasMessage("AUDIT_LOG 저장에 실패했습니다.");
    }
}
