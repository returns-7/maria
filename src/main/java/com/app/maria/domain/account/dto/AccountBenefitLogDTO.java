package com.app.maria.domain.account.dto;

import com.app.maria.domain.account.type.BenefitType;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "benefitId")
public class AccountBenefitLogDTO {
    private Long benefitId;
    private Long accountId;
    private BenefitType prevStatus;
    private BenefitType newStatus;
    private LocalDateTime changedAt;
    private String reason;
}
