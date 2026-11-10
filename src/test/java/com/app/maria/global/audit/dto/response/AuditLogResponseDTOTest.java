package com.app.maria.global.audit.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.global.audit.dto.AuditLogDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditLogResponseDTOTest {

    private AuditLogDTO.AuditLogDTOBuilder auditLogBuilder(String targetTable, String targetPk) {
        return AuditLogDTO.builder().targetTable(targetTable).targetPk(targetPk);
    }

    @Test
    @DisplayName("targetTable이 ADMIN_USER면 targetAdminName을 targetName으로 쓴다")
    void targetNameUsesTargetAdminNameForAdminUser() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(
                        auditLogBuilder("ADMIN_USER", "2").targetAdminName("이국희").build());

        assertThat(response.getTargetName()).isEqualTo("이국희");
    }

    @Test
    @DisplayName("targetTable이 ACCOUNT면 targetOwnerAccountNo를 targetName으로 쓴다")
    void targetNameUsesTargetOwnerAccountNoForAccount() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(
                        auditLogBuilder("ACCOUNT", "200")
                                .targetOwnerAccountNo("9000000001")
                                .build());

        assertThat(response.getTargetName()).isEqualTo("9000000001");
    }

    @Test
    @DisplayName("targetTable이 SYSTEM_CLOCK이면 targetTable 값 그대로 targetName으로 쓴다")
    void targetNameFallsBackToTargetTableForSystemClock() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(auditLogBuilder("SYSTEM_CLOCK", "1").build());

        assertThat(response.getTargetName()).isEqualTo("SYSTEM_CLOCK");
    }

    @Test
    @DisplayName("targetTable이 SETTLEMENT_BATCH면 targetBatchExecutedAt의 날짜를 targetName으로 쓴다")
    void targetNameUsesExecutedAtDateForSettlementBatch() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(
                        auditLogBuilder("SETTLEMENT_BATCH", "5")
                                .targetBatchExecutedAt(LocalDateTime.of(2026, 8, 13, 9, 0))
                                .build());

        assertThat(response.getTargetName()).isEqualTo("2026-08-13");
    }

    @Test
    @DisplayName("targetTable이 SETTLEMENT_BATCH인데 targetBatchExecutedAt이 없으면 targetPk로 대체한다")
    void targetNameFallsBackToTargetPkForSettlementBatchWithoutExecutedAt() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(auditLogBuilder("SETTLEMENT_BATCH", "5").build());

        assertThat(response.getTargetName()).isEqualTo("5");
    }

    @Test
    @DisplayName("targetTable이 SETTLEMENT_ITEM이면 targetPk를 targetName으로 쓴다")
    void targetNameUsesTargetPkForSettlementItem() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(auditLogBuilder("SETTLEMENT_ITEM", "46").build());

        assertThat(response.getTargetName()).isEqualTo("46");
    }

    @Test
    @DisplayName("알려지지 않은 targetTable 값이 와도 하드코딩된 값이 아니라 실제 targetTable을 그대로 targetName으로 쓴다")
    void targetNameFallsBackToActualTargetTableForUnknownType() {
        AuditLogResponseDTO response =
                new AuditLogResponseDTO(auditLogBuilder("SOME_FUTURE_TYPE", "1").build());

        assertThat(response.getTargetName()).isEqualTo("SOME_FUTURE_TYPE");
    }
}
