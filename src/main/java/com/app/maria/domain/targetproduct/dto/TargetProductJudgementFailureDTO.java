package com.app.maria.domain.targetproduct.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductJudgementFailureDTO {
    private Long failureId;
    private Long mydataTradeId;
    private String ciHash;
    private LocalDate tradeDate;
    private int failureCount;
    private String lastError;
    private LocalDateTime firstFailedAt;
    private LocalDateTime lastFailedAt;
}
