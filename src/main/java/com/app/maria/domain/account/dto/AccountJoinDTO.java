package com.app.maria.domain.account.dto;

import com.app.maria.domain.account.type.BenefitType;
import com.app.maria.domain.account.type.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountJoinDTO {
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
}
