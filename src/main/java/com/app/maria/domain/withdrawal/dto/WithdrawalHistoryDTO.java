package com.app.maria.domain.withdrawal.dto;

import com.app.maria.domain.withdrawal.type.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalHistoryDTO {
    private Long withdrawalId;
    private Long accountId;
    private String customerName;
    private String riaAccountNo;
    private BigDecimal requestedAmount;
    private LocalDateTime processedAt;
    private String destinationAccountNo;
    private Long destinationGeneralAccountId;
    private WithdrawalStatus status;
    private BigDecimal earningsAmount;
    private BigDecimal maturedPrincipalAmount;
    private BigDecimal immaturePrincipalAmount;
    private int allocationCount;
    private int normalAllocationCount;
    private int earlyAllocationCount;
}
