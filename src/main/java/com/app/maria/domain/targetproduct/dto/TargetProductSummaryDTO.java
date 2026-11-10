package com.app.maria.domain.targetproduct.dto;

import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductSummaryDTO {
    private int todayJudgementCount;
    private int todayTargetCount;
    private BigDecimal todayTargetNetBuyAmount;
    private int totalJudgementCount;
    private int todayInheritanceGiftCount;
}
