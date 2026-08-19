package com.app.maria.domain.sellorder.dto.response;

import com.app.maria.domain.sellorder.dto.SellOrderSummaryDTO;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderSummaryResponseDTO {

    private BigDecimal todaySellAmount;
    private BigDecimal todaySellAmountChangeRate;
    private int todayExecutedCount;
    private BigDecimal todayExecutedCountChangeRate;
    private BigDecimal pendingProvisionalAmount;
    private BigDecimal todayFinalizedAmount;

    public SellOrderSummaryResponseDTO(SellOrderSummaryDTO dto) {
        this.todaySellAmount = dto.getTodaySellAmount();
        this.todaySellAmountChangeRate = dto.getTodaySellAmountChangeRate();
        this.todayExecutedCount = dto.getTodayExecutedCount();
        this.todayExecutedCountChangeRate = dto.getTodayExecutedCountChangeRate();
        this.pendingProvisionalAmount = dto.getPendingProvisionalAmount();
        this.todayFinalizedAmount = dto.getTodayFinalizedAmount();
    }
}
