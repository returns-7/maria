package com.app.maria.domain.withdrawal.dto.response;

import com.app.maria.domain.withdrawal.dto.WithdrawalHistoryDTO;
import com.app.maria.domain.withdrawal.type.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalListResponseDTO {
    private Long withdrawalId;
    private String customerName;
    private String riaAccountNo;
    private BigDecimal requestedAmount;
    private String destinationAccountNo;
    private WithdrawalStatus status;
    private LocalDateTime processedAt;
    private BigDecimal earningsAmount;
    private BigDecimal maturedPrincipalAmount;
    private BigDecimal immaturePrincipalAmount;
    private int allocationCount;
    private int normalAllocationCount;
    private int earlyAllocationCount;

    public static WithdrawalListResponseDTO from(WithdrawalHistoryDTO withdrawal) {
        return WithdrawalListResponseDTO.builder()
                .withdrawalId(withdrawal.getWithdrawalId())
                .customerName(withdrawal.getCustomerName())
                .riaAccountNo(withdrawal.getRiaAccountNo())
                .requestedAmount(withdrawal.getRequestedAmount())
                .destinationAccountNo(withdrawal.getDestinationAccountNo())
                .status(withdrawal.getStatus())
                .processedAt(withdrawal.getProcessedAt())
                .earningsAmount(withdrawal.getEarningsAmount())
                .maturedPrincipalAmount(withdrawal.getMaturedPrincipalAmount())
                .immaturePrincipalAmount(withdrawal.getImmaturePrincipalAmount())
                .allocationCount(withdrawal.getAllocationCount())
                .normalAllocationCount(withdrawal.getNormalAllocationCount())
                .earlyAllocationCount(withdrawal.getEarlyAllocationCount())
                .build();
    }
}
