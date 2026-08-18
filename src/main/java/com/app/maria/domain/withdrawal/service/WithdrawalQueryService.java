package com.app.maria.domain.withdrawal.service;

import com.app.maria.domain.withdrawal.dto.response.WithdrawalDetailResponseDTO;
import com.app.maria.domain.withdrawal.dto.response.WithdrawalListResponseDTO;
import com.app.maria.domain.withdrawal.type.WithdrawalStatus;
import java.util.List;

public interface WithdrawalQueryService {
    List<WithdrawalListResponseDTO> getWithdrawals(WithdrawalStatus status);

    List<WithdrawalListResponseDTO> getWithdrawalsByAccountId(Long accountId);

    WithdrawalDetailResponseDTO getWithdrawal(Long withdrawalId);
}
