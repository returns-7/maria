package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundPriorApprovalDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundPriorApprovalResponseDTO {
    private Long inboundId;
    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
    private LocalDateTime processedAt;

    public InboundPriorApprovalResponseDTO(InboundPriorApprovalDTO dto) {
        this.inboundId = dto.getInboundId();
        this.requestedQty = dto.getRequestedQty();
        this.approvedQty = dto.getApprovedQty();
        this.processedAt = dto.getProcessedAt();
    }
}
