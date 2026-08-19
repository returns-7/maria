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
public class InboundPriorApprovalDTO {
    private Long inboundId;
    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
    private LocalDateTime processedAt;
}
