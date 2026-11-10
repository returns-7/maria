package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticInvestmentListDTO;
import java.math.BigDecimal;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticInvestmentListResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private BigDecimal cashAmount;
    private int holdingCount;
    private boolean hasRestrictedHolding;

    public DomesticInvestmentListResponseDTO(DomesticInvestmentListDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.cashAmount = dto.getCashAmount();
        this.holdingCount = dto.getHoldingCount();
        this.hasRestrictedHolding = dto.isHasRestrictedHolding();
    }
}
