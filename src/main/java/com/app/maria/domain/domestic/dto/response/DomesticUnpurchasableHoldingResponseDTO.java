package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticFundHoldingDetailDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticUnpurchasableHoldingResponseDTO {
    private String customerName;
    private String accountNo;
    private String productName;
    private String ticker;
    private BigDecimal domesticStockRatio;
    private LocalDate inceptionDate;

    public DomesticUnpurchasableHoldingResponseDTO(DomesticFundHoldingDetailDTO dto) {
        this.customerName = dto.getCustomerName();
        this.accountNo = dto.getAccountNo();
        this.productName = dto.getProductName();
        this.ticker = dto.getTicker();
        this.domesticStockRatio = dto.getDomesticStockRatio();
        this.inceptionDate = dto.getInceptionDate();
    }
}
