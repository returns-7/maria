package com.app.maria.domain.inbound.dto;

import com.app.maria.domain.registrablestock.type.GeneralAccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundLotDTO {
    private Long inboundId;
    private Long inboundDetailId;
    private String sourceBroker;
    private BigDecimal qty;
    private BigDecimal currentQty;
    private LocalDateTime recordedAt;
    private GeneralAccountType accountType;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;
    private List<InboundSellHistoryDTO> sellHistory;
}
