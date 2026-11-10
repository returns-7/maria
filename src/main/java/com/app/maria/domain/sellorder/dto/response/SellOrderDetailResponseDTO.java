package com.app.maria.domain.sellorder.dto.response;

import com.app.maria.domain.sellorder.dto.SellOrderDetailDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderDetailResponseDTO {

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

    public SellOrderDetailResponseDTO(SellOrderDetailDTO dto) {
        this.orderId = dto.getOrderId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.ticker = dto.getTicker();
        this.name = dto.getName();
        this.sellQty = dto.getSellQty();
        this.basePrice = dto.getBasePrice();
        this.settlementFxRate = dto.getSettlementFxRate();
        this.processedAt = dto.getProcessedAt();
        this.status = dto.getStatus();
        this.provisionalAmount = dto.getProvisionalAmount();
        this.provisionalAt = dto.getProvisionalAt();
        this.finalRate = dto.getFinalRate();
        this.finalAmount = dto.getFinalAmount();
        this.finalAt = dto.getFinalAt();
        this.settlementStatus = dto.getSettlementStatus();
        this.sourceBroker = dto.getSourceBroker();
        this.purchaseDate = dto.getPurchaseDate();
        this.purchasePrice = dto.getPurchasePrice();
        this.purchaseCurrency = dto.getPurchaseCurrency();
    }
}
