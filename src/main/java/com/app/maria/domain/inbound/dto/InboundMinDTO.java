package com.app.maria.domain.inbound.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundMinDTO {
    private Long inboundMinId;
    private Long inboundDetailId;
    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
    private BigDecimal snapshotQty;

    public static InboundMinDTO of(
            Long inboundDetailId,
            BigDecimal requestedQty,
            BigDecimal approvedQty,
            BigDecimal snapshotQty) {
        return InboundMinDTO.builder()
                .inboundDetailId(inboundDetailId)
                .requestedQty(requestedQty)
                .approvedQty(approvedQty)
                .snapshotQty(snapshotQty)
                .build();
    }
}
