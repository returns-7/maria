package com.app.maria.domain.domestic.dto;

import com.app.maria.domain.domestic.type.DomesticStockStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticStockBalanceDTO {
    private Long domesticStockBalanceId;
    private Long accountId;
    private Long domesticProductId;
    private BigDecimal qty;
    private DomesticStockStatus status;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal avgPurchasePrice;
}
