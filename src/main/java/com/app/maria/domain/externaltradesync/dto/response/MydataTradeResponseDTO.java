package com.app.maria.domain.externaltradesync.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode(of = "tradeId")
public class MydataTradeResponseDTO {

    private Long tradeId;
    private String ciHash;
    private String brokerName;
    private String tradeType;
    private String stockType;
    private BigDecimal qty;
    private LocalDate tradeDate;
    private BigDecimal amount;
    private String fundCode;
    private String ticker;
}
