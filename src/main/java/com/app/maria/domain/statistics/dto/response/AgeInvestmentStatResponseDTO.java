package com.app.maria.domain.statistics.dto.response;

import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AgeInvestmentStatResponseDTO {
    private String ageGroup;
    private BigDecimal purchaseAmount;
    private int purchaseCount;

    public AgeInvestmentStatResponseDTO(AgeInvestmentStatDTO dto) {
        this.ageGroup = dto.getAgeGroup();
        this.purchaseAmount = dto.getPurchaseAmount();
        this.purchaseCount = dto.getPurchaseCount();
    }
}
