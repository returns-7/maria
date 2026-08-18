package com.app.maria.domain.accountclosure.dto.response;

import com.app.maria.domain.accountclosure.dto.AccountClosureDTO;
import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountClosureDetailResponseDTO {
    private Long closureRequestId;
    private String customerName;
    private String accountNo;
    private BigDecimal accountAmount;
    private Long destinationGeneralAccountId;
    private boolean earlyWithdrawalAgreed;
    private boolean hasImmaturePrincipal;
    private BigDecimal immaturePrincipalAmount;
    private boolean taxBenefitCancellationExpected;
    private boolean taxBenefitCancellationOccurred;
    private AccountClosureStatus status;
    private LocalDateTime requestedAt;

    public static AccountClosureDetailResponseDTO from(
            AccountClosureDTO closure, BigDecimal immaturePrincipalAmount) {
        boolean hasImmaturePrincipal = immaturePrincipalAmount.compareTo(BigDecimal.ZERO) > 0;
        boolean requested = closure.getStatus() == AccountClosureStatus.REQUESTED;
        boolean completed = closure.getStatus() == AccountClosureStatus.COMPLETED;

        return AccountClosureDetailResponseDTO.builder()
                .closureRequestId(closure.getClosureRequestId())
                .customerName(closure.getCustomerName())
                .accountNo(closure.getAccountNo())
                .accountAmount(closure.getAccountAmount())
                .destinationGeneralAccountId(closure.getDestinationGeneralAccountId())
                .earlyWithdrawalAgreed(closure.isEarlyWithdrawalAgreed())
                .hasImmaturePrincipal(hasImmaturePrincipal)
                .immaturePrincipalAmount(immaturePrincipalAmount)
                .taxBenefitCancellationExpected(requested && hasImmaturePrincipal)
                .taxBenefitCancellationOccurred(completed && hasImmaturePrincipal)
                .status(closure.getStatus())
                .requestedAt(closure.getRequestedAt())
                .build();
    }
}
