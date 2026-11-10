package com.app.maria.domain.sellorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderDetailDTO {

    private Long orderId;
    private String accountNo;
    private String customerName;
    private String ticker;
    private String name;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private BigDecimal settlementFxRate;
    private LocalDateTime processedAt;
    private String status;

    private BigDecimal provisionalAmount;
    private LocalDateTime provisionalAt;
    private BigDecimal finalRate;
    private BigDecimal finalAmount;
    private LocalDateTime finalAt;
    private String settlementStatus;

    private String sourceBroker;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
}
