package com.app.maria.domain.sellorder.dto;

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
public class SellOrderSummaryDTO {

    private BigDecimal todaySellAmount;
    private BigDecimal todaySellAmountChangeRate;
    private int todayExecutedCount;
    private BigDecimal todayExecutedCountChangeRate;
    private BigDecimal pendingProvisionalAmount;
    private BigDecimal todayFinalizedAmount;
}
