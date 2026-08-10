package com.app.maria.global.audit.dto.response;

import com.app.maria.global.audit.dto.AuditLogDTO;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AuditLogResponseDTO {

    private Long auditId;
    private Long adminId;
    private String targetTable;
    private String targetPk;
    private String beforeValue;
    private String afterValue;
    private String reasonCode;
    private LocalDateTime processedAt;

    public AuditLogResponseDTO(AuditLogDTO dto) {
        this.auditId = dto.getAuditId();
        this.adminId = dto.getAdminId();
        this.targetTable = dto.getTargetTable();
        this.targetPk = dto.getTargetPk();
        this.beforeValue = dto.getBeforeValue();
        this.afterValue = dto.getAfterValue();
        this.reasonCode = dto.getReasonCode();
        this.processedAt = dto.getProcessedAt();
    }

}
