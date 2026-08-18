package com.app.maria.domain.withdrawal.service;

import com.app.maria.domain.withdrawal.dto.WithdrawalAllocationDTO;
import com.app.maria.domain.withdrawal.dto.WithdrawalResultDTO;
import com.app.maria.domain.withdrawal.dto.request.WithdrawalRequestDTO;
import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {

    List<WithdrawalAllocationDTO> withdraw(WithdrawalRequestDTO requestDTO);

    WithdrawalResultDTO withdrawForClosure(WithdrawalRequestDTO requestDTO);

    boolean hasImmaturePrincipal(Long accountId);

    BigDecimal getImmaturePrincipalAmount(Long accountId);

    BigDecimal getImmatureAllocatedAmount(Long withdrawalId);
}
