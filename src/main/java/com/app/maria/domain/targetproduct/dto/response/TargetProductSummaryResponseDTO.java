package com.app.maria.domain.targetproduct.dto.response;

import com.app.maria.domain.targetproduct.dto.TargetProductSummaryDTO;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TargetProductSummaryResponseDTO {
    private int todayJudgementCount;
    private int todayTargetCount;
    private BigDecimal todayTargetNetBuyAmount;
    private int totalJudgementCount;
    private int todayInheritanceGiftCount;

    public TargetProductSummaryResponseDTO(TargetProductSummaryDTO dto) {
        this.todayJudgementCount = dto.getTodayJudgementCount();
        this.todayTargetCount = dto.getTodayTargetCount();
        this.todayTargetNetBuyAmount = dto.getTodayTargetNetBuyAmount();
        this.totalJudgementCount = dto.getTotalJudgementCount();
        this.todayInheritanceGiftCount = dto.getTodayInheritanceGiftCount();
    }
}
