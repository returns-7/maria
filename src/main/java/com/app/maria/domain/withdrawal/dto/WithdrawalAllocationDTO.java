package com.app.maria.domain.withdrawal.dto;

import com.app.maria.domain.withdrawal.type.WithdrawalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class WithdrawalAllocationDTO {
    private Long allocationId;
    private Long withdrawalId;
    private Long leftAmountId;
    private BigDecimal allocatedAmount;
    private LocalDateTime withdrawalAt;
    private WithdrawalType type;
}
