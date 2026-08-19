package com.app.maria.domain.statistics.dto.response;

import com.app.maria.domain.statistics.dto.FxExchangeStatDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class FxExchangeStatResponseDTO {
    private LocalDate statDate;
    private BigDecimal provisionalAmount;
    private BigDecimal finalAmount;

    public FxExchangeStatResponseDTO(FxExchangeStatDTO dto) {
        this.statDate = dto.getStatDate();
        this.provisionalAmount = dto.getProvisionalAmount();
        this.finalAmount = dto.getFinalAmount();
    }
}
