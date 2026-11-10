package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticHoldingDTO;
import com.app.maria.domain.domestic.type.DomesticStockStatus;
import com.app.maria.domain.domestic.type.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticHoldingResponseDTO {
    private Long domesticProductId;
    private String ticker;
    private String name;
    private Type type;
    private BigDecimal domesticStockRatio;
    private LocalDate inceptionDate;
    private BigDecimal qty;
    private DomesticStockStatus status;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal avgPurchasePrice;
    private Boolean currentlyPurchasable;

    public DomesticHoldingResponseDTO(DomesticHoldingDTO dto) {
        this.ticker = dto.getTicker();
        this.name = dto.getName();
        this.type = dto.getType();
        this.domesticStockRatio = dto.getDomesticStockRatio();
        this.inceptionDate = dto.getInceptionDate();
        this.qty = dto.getQty();
        this.status = dto.getStatus();
        this.lastPurchaseDate = dto.getLastPurchaseDate();
        this.avgPurchasePrice = dto.getAvgPurchasePrice();
        this.currentlyPurchasable = dto.getCurrentlyPurchasable();
    }
}
