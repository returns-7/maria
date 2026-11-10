package com.app.maria.domain.domestic.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentSummaryDTO {
    private int totalAccountCount;
    private int restrictedAccountCount;
    private int unpurchasableHoldingCount;
    private BigDecimal totalCashAmount;
    private BigDecimal domesticStockAmount;
    private BigDecimal domesticFundAmount;
    private int stockHoldingAccountCount;
    private int fundHoldingAccountCount;
    private int recentBuyAccountCount;
    private int noRecentBuyAccountCount;
}
