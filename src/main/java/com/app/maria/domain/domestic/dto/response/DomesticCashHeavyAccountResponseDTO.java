package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticCashHeavyAccountDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticCashHeavyAccountResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private BigDecimal cashAmount;
    private BigDecimal investedAmount;
    private BigDecimal cashRatio;

    public DomesticCashHeavyAccountResponseDTO(DomesticCashHeavyAccountDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.cashAmount = dto.getCashAmount();
        this.investedAmount = dto.getInvestedAmount();
        BigDecimal total = dto.getCashAmount().add(dto.getInvestedAmount());
        this.cashRatio =
                total.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : dto.getCashAmount().divide(total, 4, RoundingMode.HALF_UP);
    }
}
