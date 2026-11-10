package com.app.maria.domain.account.dto.response;

import com.app.maria.domain.account.dto.AccountStatusLogDTO;
import com.app.maria.domain.account.type.Status;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode(of = "logId")
public class AccountLogResponseDTO {
    private Long logId;
    private Long accountId;
    private Status prevStatus;
    private Status newStatus;
    private LocalDateTime changedAt;
    private String reason;

    public AccountLogResponseDTO(AccountStatusLogDTO logDTO) {
        this.logId = logDTO.getLogId();
        this.accountId = logDTO.getAccountId();
        this.prevStatus = logDTO.getPrevStatus();
        this.newStatus = logDTO.getNewStatus();
        this.changedAt = logDTO.getChangedAt();
        this.reason = logDTO.getReason();
    }
}
