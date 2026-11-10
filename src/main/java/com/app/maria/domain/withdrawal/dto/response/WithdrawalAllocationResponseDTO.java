package com.app.maria.domain.withdrawal.dto.response;

import com.app.maria.domain.withdrawal.dto.WithdrawalAllocationHistoryDTO;
import com.app.maria.domain.withdrawal.type.WithdrawalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalAllocationResponseDTO {
    private Long allocationId;
    private Long leftAmountId;
    private Long exchangeId;
    private BigDecimal allocatedAmount;
    private LocalDateTime withdrawalAt;
    private WithdrawalType type;
    private LocalDateTime finalAt;
    private LocalDateTime maturityAt;
    private String productName;
    private String ticker;

    public static WithdrawalAllocationResponseDTO from(WithdrawalAllocationHistoryDTO allocation) {
        return WithdrawalAllocationResponseDTO.builder()
                .allocationId(allocation.getAllocationId())
                .leftAmountId(allocation.getLeftAmountId())
                .exchangeId(allocation.getExchangeId())
                .allocatedAmount(allocation.getAllocatedAmount())
                .withdrawalAt(allocation.getWithdrawalAt())
                .type(allocation.getType())
                .finalAt(allocation.getFinalAt())
                .maturityAt(
                        allocation.getFinalAt() == null
                                ? null
                                : allocation.getFinalAt().plusYears(1))
                .productName(allocation.getProductName())
                .ticker(allocation.getTicker())
                .build();
    }
}
