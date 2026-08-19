package com.app.maria.domain.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class FxExchangeStatDTO {
    private LocalDate statDate;
    private BigDecimal provisionalAmount;
    private BigDecimal finalAmount;
}
