package com.app.maria.domain.statistics.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AgeInvestmentStatDTO {
    private String ageGroup;
    private BigDecimal purchaseAmount;
    private int purchaseCount;
}
