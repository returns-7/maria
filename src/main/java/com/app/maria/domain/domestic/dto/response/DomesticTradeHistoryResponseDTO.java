package com.app.maria.domain.domestic.dto.response;

import com.app.maria.domain.domestic.dto.DomesticTradeHistoryDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticTradeHistoryResponseDTO {
    private String tradeType;
    private String stockCode;
    private BigDecimal qty;
    private BigDecimal price;
    private LocalDateTime executedAt;

    public DomesticTradeHistoryResponseDTO(DomesticTradeHistoryDTO dto) {
        this.tradeType = dto.getTradeType();
        this.stockCode = dto.getStockCode();
        this.qty = dto.getQty();
        this.price = dto.getPrice();
        this.executedAt = dto.getExecutedAt();
    }
}
