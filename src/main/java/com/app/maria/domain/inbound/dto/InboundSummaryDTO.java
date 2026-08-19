package com.app.maria.domain.inbound.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundSummaryDTO {
    private int todayProcessedCount;
    private int todayRejectedCount;
    private int todayReducedCount;
    private BigDecimal todayApprovedQtySum;
}
