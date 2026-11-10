package com.app.maria.domain.withdrawal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class LeftAmountDTO {
    private Long leftAmountId;
    private Long exchangeId;
    private BigDecimal curAmount;
    private LocalDateTime finalAt;
}
