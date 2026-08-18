package com.app.maria.domain.withdrawal.dto.response;

import com.app.maria.domain.withdrawal.dto.WithdrawalAllocationHistoryDTO;
import com.app.maria.domain.withdrawal.dto.WithdrawalHistoryDTO;
import com.app.maria.domain.withdrawal.type.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalDetailResponseDTO {
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
    private boolean earlyWithdrawal;
    private List<WithdrawalAllocationResponseDTO> allocations;

    public static WithdrawalDetailResponseDTO from(
            WithdrawalHistoryDTO withdrawal, List<WithdrawalAllocationHistoryDTO> allocations) {
        return WithdrawalDetailResponseDTO.builder()
                .withdrawalId(withdrawal.getWithdrawalId())
                .accountId(withdrawal.getAccountId())
                .customerName(withdrawal.getCustomerName())
                .riaAccountNo(withdrawal.getRiaAccountNo())
                .requestedAmount(withdrawal.getRequestedAmount())
                .processedAt(withdrawal.getProcessedAt())
                .destinationAccountNo(withdrawal.getDestinationAccountNo())
                .destinationGeneralAccountId(withdrawal.getDestinationGeneralAccountId())
                .status(withdrawal.getStatus())
                .earningsAmount(withdrawal.getEarningsAmount())
                .maturedPrincipalAmount(withdrawal.getMaturedPrincipalAmount())
                .immaturePrincipalAmount(withdrawal.getImmaturePrincipalAmount())
                .earlyWithdrawal(withdrawal.getImmaturePrincipalAmount().signum() > 0)
                .allocations(
                        allocations.stream().map(WithdrawalAllocationResponseDTO::from).toList())
                .build();
    }
}
