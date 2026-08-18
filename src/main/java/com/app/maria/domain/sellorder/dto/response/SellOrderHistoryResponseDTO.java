package com.app.maria.domain.sellorder.dto.response;

import com.app.maria.domain.sellorder.dto.SellOrderHistoryDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SellOrderHistoryResponseDTO {

    private String accountNo;
    private String customerName;
    private String ticker;
    private String name;
    private BigDecimal sellQty;
    private BigDecimal basePrice;
    private LocalDateTime processedAt;
    private BigDecimal provisionalAmount;
    private BigDecimal finalAmount;
    private String status;

    public SellOrderHistoryResponseDTO(SellOrderHistoryDTO dto) {
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.ticker = dto.getTicker();
        this.name = dto.getName();
        this.sellQty = dto.getSellQty();
        this.basePrice = dto.getBasePrice();
        this.processedAt = dto.getProcessedAt();
        this.provisionalAmount = dto.getProvisionalAmount();
        this.finalAmount = dto.getFinalAmount();
        this.status = dto.getStatus();
    }
}
