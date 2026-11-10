package com.app.maria.domain.withdrawal.dto;

import com.app.maria.domain.withdrawal.type.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class WithdrawalDTO {
    private Long withdrawalId;
    private Long accountId;
    private BigDecimal requestedAmount;
    private LocalDateTime processedAt;
    private String destinationAccountNo;
    private WithdrawalStatus status;
    private Long destinationGeneralAccountId;
}
