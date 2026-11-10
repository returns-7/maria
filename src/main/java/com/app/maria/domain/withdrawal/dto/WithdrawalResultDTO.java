package com.app.maria.domain.withdrawal.dto;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WithdrawalResultDTO {
    private Long withdrawalId;
    private List<WithdrawalAllocationDTO> allocations;
}
