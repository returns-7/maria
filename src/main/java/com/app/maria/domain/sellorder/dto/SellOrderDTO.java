package com.app.maria.domain.sellorder.dto;

import com.app.maria.domain.sellorder.type.SellOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "orderId")
public class SellOrderDTO {

    private Long orderId;
    private Long inboundDetailId;
    private Long accountId;
    private Long foreignProductId;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private BigDecimal settlementFxRate;
    private SellOrderStatus status;
    private LocalDateTime processedAt;
}
