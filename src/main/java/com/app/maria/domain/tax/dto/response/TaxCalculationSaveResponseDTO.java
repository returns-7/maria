package com.app.maria.domain.tax.dto.response;

import com.app.maria.domain.tax.dto.TaxCalculationDTO;
import com.app.maria.domain.tax.type.TaxBasisType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxCalculationSaveResponseDTO {
    private Long calcId;
    private Long accountId;
    private TaxBasisType basisType;
    private LocalDateTime calculatedAt;
    private BigDecimal weightedSell;
    private BigDecimal originalGainAmount;
    private BigDecimal weightedGain;
    private BigDecimal weightedExternalAmount;
    private BigDecimal adjustRatio;
    private BigDecimal finalDeduction;
    private BigDecimal finalTax;

    public static TaxCalculationSaveResponseDTO of(TaxCalculationDTO calculation) {
        return TaxCalculationSaveResponseDTO.builder()
                .calcId(calculation.getCalcId())
                .accountId(calculation.getAccountId())
                .basisType(calculation.getBasisType())
                .calculatedAt(calculation.getCalculatedAt())
                .weightedSell(calculation.getWeightedSell())
                .originalGainAmount(calculation.getOriginalGainAmount())
                .weightedGain(calculation.getWeightedGain())
                .weightedExternalAmount(calculation.getWeightedExternalAmount())
                .adjustRatio(calculation.getAdjustRatio())
                .finalDeduction(calculation.getFinalDeduction())
                .finalTax(calculation.getFinalTax())
                .build();
    }
}
