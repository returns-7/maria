package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticInvestmentSummaryDTO;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentSummaryResponseDTO {
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

    public DomesticInvestmentSummaryResponseDTO(DomesticInvestmentSummaryDTO dto) {
        this.totalAccountCount = dto.getTotalAccountCount();
        this.restrictedAccountCount = dto.getRestrictedAccountCount();
        this.unpurchasableHoldingCount = dto.getUnpurchasableHoldingCount();
        this.totalCashAmount = dto.getTotalCashAmount();
        this.domesticStockAmount = dto.getDomesticStockAmount();
        this.domesticFundAmount = dto.getDomesticFundAmount();
        this.stockHoldingAccountCount = dto.getStockHoldingAccountCount();
        this.fundHoldingAccountCount = dto.getFundHoldingAccountCount();
        this.recentBuyAccountCount = dto.getRecentBuyAccountCount();
        this.noRecentBuyAccountCount = dto.getNoRecentBuyAccountCount();
    }
}
