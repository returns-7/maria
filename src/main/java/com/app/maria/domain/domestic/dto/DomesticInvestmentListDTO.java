package com.app.maria.domain.domestic.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentListDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private BigDecimal cashAmount;
    private int holdingCount;
    private boolean hasRestrictedHolding;
}
