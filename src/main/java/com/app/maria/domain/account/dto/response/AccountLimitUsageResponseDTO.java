package com.app.maria.domain.account.dto.response;

import com.app.maria.domain.account.dto.AccountLimitUsageDTO;
import com.app.maria.domain.account.type.Status;
import java.math.BigDecimal;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class AccountLimitUsageResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private Status status;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;

    public AccountLimitUsageResponseDTO(AccountLimitUsageDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.status = dto.getStatus();
        this.limitAmount = dto.getLimitAmount();
        this.usedAmount = dto.getUsedAmount();
    }
}
