package com.app.maria.domain.tax.dto;

import com.app.maria.domain.tax.type.TaxBasisType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxCalculationDTO {
    private Long calcId;
    private Long accountId;
    private LocalDateTime calculatedAt;
    private TaxBasisType basisType;
    private BigDecimal weightedSell;
    private BigDecimal originalGainAmount;
    private BigDecimal weightedGain;
    private BigDecimal weightedExternalAmount;
    private BigDecimal adjustRatio;
    private BigDecimal finalDeduction;
    private BigDecimal finalTax;

    public static TaxCalculationDTO of(
            Long accountId,
            TaxBasisType basisType,
            LocalDateTime calculatedAt,
            TaxCalculationResultDTO result) {
        return TaxCalculationDTO.builder()
                .accountId(accountId)
                .basisType(basisType)
                .calculatedAt(calculatedAt)
                .weightedSell(result.getWeightedSell())
                .originalGainAmount(result.getOriginalGainAmount())
                .weightedGain(result.getWeightedGain())
                .weightedExternalAmount(result.getWeightedExternalAmount())
                .adjustRatio(result.getAdjustRatio())
                .finalDeduction(result.getFinalDeduction())
                .finalTax(result.getFinalTax())
                .build();
    }
}
