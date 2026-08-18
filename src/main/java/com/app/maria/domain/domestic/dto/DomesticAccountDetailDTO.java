package com.app.maria.domain.domestic.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticAccountDetailDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private BigDecimal cashAmount;
    private List<DomesticHoldingDTO> holdings;
    private List<DomesticTradeHistoryDTO> tradeHistory;
}
