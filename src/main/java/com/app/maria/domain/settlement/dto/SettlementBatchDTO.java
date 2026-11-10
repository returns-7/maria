package com.app.maria.domain.settlement.dto;

import com.app.maria.domain.settlement.type.BatchStatus;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "batchId")
public class SettlementBatchDTO {
    private Long batchId;
    private LocalDateTime executedAt;
    private BatchStatus status;
    private String runId;
    private String failureMessage;
    private int totalCount;
    private int successCount;
    private int failedCount;
    private int processedCount;
}
