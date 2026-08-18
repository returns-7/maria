package com.app.maria.domain.withdrawal.dto;

import com.app.maria.domain.withdrawal.type.WithdrawalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalAllocationHistoryDTO {
    private Long allocationId;
    private Long leftAmountId;
    private Long exchangeId;
    private BigDecimal allocatedAmount;
    private LocalDateTime withdrawalAt;
    private WithdrawalType type;
    private LocalDateTime finalAt;
    private String productName;
    private String ticker;
}
