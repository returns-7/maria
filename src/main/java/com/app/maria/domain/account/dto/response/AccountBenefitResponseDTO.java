package com.app.maria.domain.account.dto.response;

import com.app.maria.domain.account.dto.AccountBenefitLogDTO;
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
public class AccountBenefitResponseDTO {
    private Long benefitId;
    private Long accountId;
    private BenefitType prevStatus;
    private BenefitType newStatus;
    private LocalDateTime changedAt;
    private String reason;

    public AccountBenefitResponseDTO(AccountBenefitLogDTO logDTO) {
        this.benefitId = logDTO.getBenefitId();
        this.accountId = logDTO.getAccountId();
        this.prevStatus = logDTO.getPrevStatus();
        this.newStatus = logDTO.getNewStatus();
        this.changedAt = logDTO.getChangedAt();
        this.reason = logDTO.getReason();
    }
}
