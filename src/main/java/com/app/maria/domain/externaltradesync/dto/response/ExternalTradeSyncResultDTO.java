package com.app.maria.domain.externaltradesync.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ExternalTradeSyncResultDTO {
    private int customerCount;
    private int failedCustomerCount;
    private int newJudgementCount;
    private int skippedJudgementCount;
    private int permanentlyFailedJudgementCount;
    private List<Long> newJudgementIds;
}
