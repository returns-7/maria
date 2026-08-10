package com.app.maria.global.audit.dto.request;

import com.app.maria.global.audit.dto.AuditLogSearchDTO;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AuditLogSearchRequestDTO {

    private Long adminId;
    private String targetTable;
    private String targetPk;
    private String reasonCode;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @AssertTrue(message = "시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }

    public AuditLogSearchDTO toAuditLogSearchDTO() {
        return AuditLogSearchDTO.builder()
                .adminId(adminId)
                .targetTable(targetTable)
                .targetPk(targetPk)
                .reasonCode(reasonCode)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

}
