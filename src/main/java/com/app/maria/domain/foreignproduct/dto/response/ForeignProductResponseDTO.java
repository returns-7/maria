package com.app.maria.domain.foreignproduct.dto.response;

import com.app.maria.domain.foreignproduct.dto.ForeignProductDTO;
import com.app.maria.domain.foreignproduct.type.ForeignProductType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ForeignProductResponseDTO {
    private Long foreignProductId;
    private String ticker;
    private String name;
    private String market;
    private String currency;
    private ForeignProductType foreignProductType;

    public ForeignProductResponseDTO(ForeignProductDTO dto) {
        this.foreignProductId = dto.getForeignProductId();
        this.ticker = dto.getTicker();
        this.name = dto.getName();
        this.market = dto.getMarket();
        this.currency = dto.getCurrency();
        this.foreignProductType = dto.getForeignProductType();
    }
}
