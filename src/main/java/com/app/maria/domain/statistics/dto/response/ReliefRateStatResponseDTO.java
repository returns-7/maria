package com.app.maria.domain.statistics.dto.response;

import com.app.maria.domain.statistics.dto.ReliefRateStatDTO;
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
public class ReliefRateStatResponseDTO {
    private String periodLabel;
    private BigDecimal sellAmount;

    public ReliefRateStatResponseDTO(ReliefRateStatDTO dto) {
        this.periodLabel = dto.getPeriodLabel();
        this.sellAmount = dto.getSellAmount();
    }
}
