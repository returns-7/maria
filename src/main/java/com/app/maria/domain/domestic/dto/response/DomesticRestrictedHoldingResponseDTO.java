package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticRestrictedHoldingDTO;
import com.app.maria.domain.domestic.type.DomesticStockStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticRestrictedHoldingResponseDTO {
    private String customerName;
    private String accountNo;
    private String productName;
    private String ticker;
    private DomesticStockStatus status;

    public DomesticRestrictedHoldingResponseDTO(DomesticRestrictedHoldingDTO dto) {
        this.customerName = dto.getCustomerName();
        this.accountNo = dto.getAccountNo();
        this.productName = dto.getProductName();
        this.ticker = dto.getTicker();
        this.status = dto.getStatus();
    }
}
