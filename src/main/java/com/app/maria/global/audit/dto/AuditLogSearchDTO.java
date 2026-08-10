package com.app.maria.global.audit.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AuditLogSearchDTO {

    private Long adminId;
    private String targetTable;
    private String targetPk;
    private String reasonCode;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
