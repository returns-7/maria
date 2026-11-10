package com.app.maria.domain.accountclosure.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountClosureApplyRequestDTO {
    @NotNull @Positive private Long destinationGeneralAccountId;
    private boolean earlyWithdrawalAgreed;
    @NotNull @Positive private Long customerId;
}
