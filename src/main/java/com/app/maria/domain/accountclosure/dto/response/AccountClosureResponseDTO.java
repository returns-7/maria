package com.app.maria.domain.accountclosure.dto.response;

import com.app.maria.domain.accountclosure.dto.AccountClosureDTO;
import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AccountClosureResponseDTO {
    private Long closureRequestId;
    private String customerName;
    private String accountNo;
    private AccountClosureStatus status;
    private LocalDateTime requestedAt;

    public static AccountClosureResponseDTO from(AccountClosureDTO closure) {
        return AccountClosureResponseDTO.builder()
                .closureRequestId(closure.getClosureRequestId())
                .customerName(closure.getCustomerName())
                .accountNo(closure.getAccountNo())
                .status(closure.getStatus())
                .requestedAt(closure.getRequestedAt())
                .build();
    }
}
