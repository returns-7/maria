package com.app.maria.domain.inbound.dto;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceLotApprovedQtyDTO {
    private Long generalAccountId;
    private BigDecimal approvedQty;
}
