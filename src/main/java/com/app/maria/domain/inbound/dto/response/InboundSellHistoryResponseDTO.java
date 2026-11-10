package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundSellHistoryDTO;
import com.app.maria.domain.sellorder.type.SellOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InboundSellHistoryResponseDTO {
    private Long inboundDetailId;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private SellOrderStatus status;
    private LocalDateTime processedAt;

    public InboundSellHistoryResponseDTO(InboundSellHistoryDTO dto) {
        this.sellQty = dto.getSellQty();
        this.basePrice = dto.getBasePrice();
        this.status = dto.getStatus();
        this.processedAt = dto.getProcessedAt();
    }
}
