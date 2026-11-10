package com.app.maria.domain.account.dto;

import com.app.maria.domain.account.type.Status;
import java.math.BigDecimal;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AccountLimitUsageDTO {

    private Long accountId;
    private String accountNo;
    private String customerName;
    private Status status;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;
}
