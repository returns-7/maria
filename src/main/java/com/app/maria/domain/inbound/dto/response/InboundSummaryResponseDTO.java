package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundSummaryDTO;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundSummaryResponseDTO {
    private int todayProcessedCount;
    private int todayRejectedCount;
    private int todayReducedCount;
    private BigDecimal todayApprovedQtySum;

    public InboundSummaryResponseDTO(InboundSummaryDTO dto) {
        this.todayProcessedCount = dto.getTodayProcessedCount();
        this.todayRejectedCount = dto.getTodayRejectedCount();
        this.todayReducedCount = dto.getTodayReducedCount();
        this.todayApprovedQtySum = dto.getTodayApprovedQtySum();
    }
}
