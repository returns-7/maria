package com.app.maria.domain.inbound.dto;

import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InboundAccountSummaryDTO {
    private Long accountId;
    private String accountNo;
    private String customerName;
    private int inboundCount;
    private LocalDateTime lastProcessedAt;
}
