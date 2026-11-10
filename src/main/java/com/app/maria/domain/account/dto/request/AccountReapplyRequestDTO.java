package com.app.maria.domain.account.dto.request;

import com.app.maria.domain.account.dto.AccountDTO;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountReapplyRequestDTO {

    @DecimalMin(value = "1", message = "계좌 한도는 1원 이상이어야 합니다.")
    @DecimalMax(value = "50000000", message = "계좌 한도는 5천만원 이하여야 합니다.")
    @Digits(integer = 8, fraction = 0, message = "계좌 한도는 원 단위 정수로 입력해야 합니다.")
    private BigDecimal limitAmount;

    public AccountDTO toAccountDTO() {
        return AccountDTO.builder().limitAmount(limitAmount).build();
    }
}
