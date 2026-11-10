package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.foreignproduct.dto.ForeignProductDTO;
import com.app.maria.domain.foreignproduct.type.ForeignProductType;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AccountHoldingResponseDTO {
    private Long foreignProductId;
    private String ticker;
    private String name;
    private String market;
    private String currency;
    private ForeignProductType type;
    private BigDecimal currentQty;

    public AccountHoldingResponseDTO(ForeignProductDTO product, BigDecimal currentQty) {
        this.foreignProductId = product.getForeignProductId();
        this.ticker = product.getTicker();
        this.name = product.getName();
        this.market = product.getMarket();
        this.currency = product.getCurrency();
        this.type = product.getForeignProductType();
        this.currentQty = currentQty;
    }
}
