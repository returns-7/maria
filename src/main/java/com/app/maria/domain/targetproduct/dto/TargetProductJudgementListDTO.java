package com.app.maria.domain.targetproduct.dto;

import com.app.maria.domain.targetproduct.type.StockType;
import com.app.maria.domain.targetproduct.type.TradeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductJudgementListDTO {
    private Long judgementId;
    private String customerName;
    private StockType stockType;
    private String fundName;
    private String ticker;
    private Boolean isTarget;
    private BigDecimal foreignStockRatio;
    private LocalDate inceptionDate;
    private TradeType tradeType;
    private BigDecimal amount;
    private BigDecimal netBuyAmount;
    private LocalDate tradeDate;
    private LocalDateTime judgedAt;
}
