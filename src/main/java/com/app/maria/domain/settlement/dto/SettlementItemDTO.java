package com.app.maria.domain.settlement.dto;

import com.app.maria.domain.settlement.type.SettlementFailureCode;
import com.app.maria.domain.settlement.type.SettlementItemResult;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "itemId")
public class SettlementItemDTO {
    private Long itemId;
    private Long batchId;
    private Long exchangeId;
    private SettlementItemResult result;
    private LocalDateTime processedAt;
    private SettlementFailureCode failureCode;
    private String failureMessage;
}
