package com.app.maria.domain.tax.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RiaSellAggregateDTO {
    private final BigDecimal weightedSell;
    private final BigDecimal weightedGain;
    private final BigDecimal originalGainAmount;

    public static RiaSellAggregateDTO of(
            BigDecimal weightedSell, BigDecimal weightedGain, BigDecimal originalGainAmount) {
        return RiaSellAggregateDTO.builder()
                .weightedSell(weightedSell)
                .weightedGain(weightedGain)
                .originalGainAmount(originalGainAmount)
                .build();
    }
}
