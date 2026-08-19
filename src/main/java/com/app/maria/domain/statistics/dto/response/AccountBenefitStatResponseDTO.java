package com.app.maria.domain.statistics.dto.response;

import com.app.maria.domain.statistics.dto.AccountBenefitStatDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AccountBenefitStatResponseDTO {
    private String benefit;
    private int accountCount;

    public AccountBenefitStatResponseDTO(AccountBenefitStatDTO dto) {
        this.benefit = dto.getBenefit();
        this.accountCount = dto.getAccountCount();
    }
}
