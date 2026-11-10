package com.app.maria.domain.account.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLimitUpdateRequestDTO {
    @NotNull(message = "사용자 ID는 필수입니다.")
    @Positive(message = "사용자 ID는 0보다 커야 합니다.")
    private Long customerId;

    @NotNull(message = "현재 계좌 한도 입력이 필요합니다.")
    @DecimalMin(value = "1", message = "현재 계좌 한도는 1원 이상이어야 합니다.")
    @DecimalMax(value = "50000000", message = "현재 계좌 한도는 5천만원 이하여야 합니다.")
    @Digits(integer = 8, fraction = 0, message = "현재 계좌 한도는 원 단위 정수로 입력해야 합니다.")
    private BigDecimal expectedCurrentLimit;

    @NotNull(message = "계좌 한도는 필수입니다.")
    @DecimalMin(value = "1", message = "계좌 한도는 1원 이상이어야 합니다.")
    @DecimalMax(value = "50000000", message = "계좌 한도는 5천만원 이하여야 합니다.")
    @Digits(integer = 8, fraction = 0, message = "계좌 한도는 원 단위 정수로 입력해야 합니다.")
    private BigDecimal limitAmount;
}
