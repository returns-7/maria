package com.app.maria.domain.inbound.dto;

import com.app.maria.domain.sellorder.type.SellOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InboundSellHistoryDTO {
    private Long inboundDetailId;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private SellOrderStatus status;
    private LocalDateTime processedAt;
}
