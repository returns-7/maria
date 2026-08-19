package com.app.maria.domain.statistics.dto.response;

import com.app.maria.domain.statistics.dto.ProductPurchaseStatDTO;
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
public class ProductPurchaseStatResponseDTO {
    private Long domesticProductId;
    private String ticker;
    private String name;
    private BigDecimal purchaseAmount;
    private int purchaseCount;

    public ProductPurchaseStatResponseDTO(ProductPurchaseStatDTO dto) {
        this.domesticProductId = dto.getDomesticProductId();
        this.ticker = dto.getTicker();
        this.name = dto.getName();
        this.purchaseAmount = dto.getPurchaseAmount();
        this.purchaseCount = dto.getPurchaseCount();
    }
}
