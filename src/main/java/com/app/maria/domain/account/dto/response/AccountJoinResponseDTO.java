package com.app.maria.domain.account.dto.response;

import com.app.maria.domain.account.dto.AccountJoinDTO;
import com.app.maria.domain.account.type.BenefitType;
import com.app.maria.domain.account.type.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountJoinResponseDTO {
    private Long accountId;
    private Long customerId;
    private String customerName;
    private String accountNo;
    private Status status;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime openedAt;
    private BenefitType benefit;

    public AccountJoinResponseDTO(AccountJoinDTO joinDTO) {
        this.accountId = joinDTO.getAccountId();
        this.customerId = joinDTO.getCustomerId();
        this.customerName = joinDTO.getCustomerName();
        this.accountNo = joinDTO.getAccountNo();
        this.status = joinDTO.getStatus();
        this.limitAmount = joinDTO.getLimitAmount();
        this.usedAmount = joinDTO.getUsedAmount();
        this.amount = joinDTO.getAmount();
        this.createdAt = joinDTO.getCreatedAt();
        this.openedAt = joinDTO.getOpenedAt();
        this.benefit = joinDTO.getBenefit();
    }
}
