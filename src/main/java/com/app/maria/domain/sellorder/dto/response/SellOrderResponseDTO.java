package com.app.maria.domain.sellorder.dto.response;

import com.app.maria.domain.sellorder.dto.SellOrderDTO;
import com.app.maria.domain.sellorder.type.SellOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderResponseDTO {

    private Long orderId;
    private Long inboundDetailId;
    private Long accountId;
    private Long foreignProductId;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private BigDecimal settlementFxRate;
    private SellOrderStatus status;
    private LocalDateTime processedAt;

    public SellOrderResponseDTO(SellOrderDTO dto) {
        this.orderId = dto.getOrderId();
        this.inboundDetailId = dto.getInboundDetailId();
        this.accountId = dto.getAccountId();
        this.foreignProductId = dto.getForeignProductId();
        this.sellQty = dto.getSellQty();
        this.basePrice = dto.getBasePrice();
        this.settlementFxRate = dto.getSettlementFxRate();
        this.status = dto.getStatus();
        this.processedAt = dto.getProcessedAt();
    }
}
