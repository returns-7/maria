package com.app.maria.domain.accountclosure.dto;

import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountClosureDTO {
    private Long closureRequestId;
    private Long accountId;
    private Long destinationGeneralAccountId;
    private boolean earlyWithdrawalAgreed;
    private AccountClosureStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedBy;
    private String rejectionReason;
    private Long withdrawalId;
    private String customerName;
    private String accountNo;
    private BigDecimal accountAmount;
}
