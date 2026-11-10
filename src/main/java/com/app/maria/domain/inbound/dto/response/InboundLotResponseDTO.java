package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundLotDTO;
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
public class InboundLotResponseDTO {
    private String sourceBroker;
    private BigDecimal qty;
    private BigDecimal currentQty;
    private LocalDateTime recordedAt;
    private GeneralAccountType accountType;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;
    private List<InboundSellHistoryResponseDTO> sellHistory;

    public InboundLotResponseDTO(InboundLotDTO dto) {
        this.sourceBroker = dto.getSourceBroker();
        this.qty = dto.getQty();
        this.currentQty = dto.getCurrentQty();
        this.recordedAt = dto.getRecordedAt();
        this.accountType = dto.getAccountType();
        this.purchaseDate = dto.getPurchaseDate();
        this.purchasePrice = dto.getPurchasePrice();
        this.purchaseCurrency = dto.getPurchaseCurrency();
        this.purchaseFxRate = dto.getPurchaseFxRate();
        this.sellHistory =
                dto.getSellHistory() == null
                        ? List.of()
                        : dto.getSellHistory().stream()
                                .map(InboundSellHistoryResponseDTO::new)
                                .toList();
    }
}
