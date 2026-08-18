package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticAccountDetailDTO;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticAccountDetailResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private BigDecimal cashAmount;
    private List<DomesticHoldingResponseDTO> holdings;
    private List<DomesticTradeHistoryResponseDTO> tradeHistory;

    public DomesticAccountDetailResponseDTO(DomesticAccountDetailDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.cashAmount = dto.getCashAmount();
        this.holdings = dto.getHoldings().stream().map(DomesticHoldingResponseDTO::new).toList();
        this.tradeHistory =
                dto.getTradeHistory().stream().map(DomesticTradeHistoryResponseDTO::new).toList();
    }
}
