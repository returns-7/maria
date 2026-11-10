package com.app.maria.domain.settlement.dto;

import com.app.maria.domain.settlement.type.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "exchangeId")
public class KrwExchangeDTO {
    private Long exchangeId;
    private Long accountId;
    private Long orderId;
    private BigDecimal provisionalAmount;
    private LocalDateTime provisionalAt;
    private BigDecimal finalRate;
    private BigDecimal finalAmount;
    private LocalDateTime finalAt;
    private SettlementStatus settlementStatus;
}
