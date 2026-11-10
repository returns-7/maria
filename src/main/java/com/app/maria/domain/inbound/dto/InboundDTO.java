package com.app.maria.domain.inbound.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundDTO {
    private Long inboundId;
    private Long accountId;
    private BigDecimal requestedQty;
    private BigDecimal currentHoldingAtRequest;
    private BigDecimal approvedQty;
    private LocalDateTime processedAt;
}
