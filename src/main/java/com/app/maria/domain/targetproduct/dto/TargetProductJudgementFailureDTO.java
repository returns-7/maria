package com.app.maria.domain.targetproduct.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
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
