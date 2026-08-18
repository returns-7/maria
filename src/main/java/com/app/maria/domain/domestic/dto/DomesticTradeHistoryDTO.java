package com.app.maria.domain.domestic.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticTradeHistoryDTO {
    private String tradeType;
    private String stockCode;
    private BigDecimal qty;
    private BigDecimal price;
    private LocalDateTime executedAt;
}
