package com.app.maria.domain.domestic.dto;

import com.app.maria.domain.domestic.type.DomesticStockStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticRestrictedHoldingDTO {
    private String customerName;
    private String accountNo;
    private String productName;
    private String ticker;
    private DomesticStockStatus status;
}
