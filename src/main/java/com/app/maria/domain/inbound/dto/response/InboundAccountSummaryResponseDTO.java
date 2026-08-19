package com.app.maria.domain.inbound.dto.response;

import com.app.maria.domain.inbound.dto.InboundAccountSummaryDTO;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundAccountSummaryResponseDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private int inboundCount;
    private LocalDateTime lastProcessedAt;

    public InboundAccountSummaryResponseDTO(InboundAccountSummaryDTO dto) {
        this.accountId = dto.getAccountId();
        this.accountNo = dto.getAccountNo();
        this.customerName = dto.getCustomerName();
        this.inboundCount = dto.getInboundCount();
        this.lastProcessedAt = dto.getLastProcessedAt();
    }
}
