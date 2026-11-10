package com.app.maria.domain.domestic.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticFundHoldingDetailDTO {
    private Long accountId;
    private String customerName;
    private String accountNo;
    private String productName;
    private String ticker;
    private BigDecimal domesticStockRatio;
    private LocalDate inceptionDate;
}
