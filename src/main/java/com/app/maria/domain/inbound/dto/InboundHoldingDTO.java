package com.app.maria.domain.inbound.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundHoldingDTO {

    private Long foreignProductId;
    private BigDecimal currentQty;
}
