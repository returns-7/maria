package com.app.maria.domain.account.dto;

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
public class AccountStatusLogDTO {
    private Long logId;
    private Long accountId;
    private Status prevStatus;
    private Status newStatus;
    private LocalDateTime changedAt;
    private String reason;
}
