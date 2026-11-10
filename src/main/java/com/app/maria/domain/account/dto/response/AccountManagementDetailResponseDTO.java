package com.app.maria.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountManagementDetailResponseDTO {
    private List<Holding> holdings;
    private List<Inbound> inbounds;
    private Closure closure;
    private List<Withdrawal> withdrawals;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Holding {
        private String ticker;
        private String productName;
        private String market;
        private String currency;
        private BigDecimal currentQty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Inbound {
        private Long inboundId;
        private String ticker;
        private String productName;
        private String sourceBroker;
        private BigDecimal qty;
        private BigDecimal currentQty;
        private LocalDate purchaseDate;
        private LocalDateTime recordedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Closure {
        private Long closureRequestId;
        private String status;
        private LocalDateTime requestedAt;
        private LocalDateTime processedAt;
        private String rejectionReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Withdrawal {
        private Long withdrawalId;
        private BigDecimal requestedAmount;
        private String destinationAccountNo;
        private String status;
        private LocalDateTime processedAt;
    }
}
