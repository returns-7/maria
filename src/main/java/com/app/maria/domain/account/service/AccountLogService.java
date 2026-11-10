package com.app.maria.domain.account.service;

import com.app.maria.domain.account.dto.AccountBenefitLogDTO;
import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.dto.response.AccountLogResponseDTO;
import com.app.maria.domain.account.type.BenefitType;
import com.app.maria.domain.account.type.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountLogService {
    void recordStatusChange(
            AccountDTO account, Status previousStatus, LocalDateTime changedAt, String reason);

    List<AccountLogResponseDTO> getStatusLogs(Long accountId);

    String createLimitChangeReason(BigDecimal before, BigDecimal after);

    /**
     * @param account 변경 후의 accountDTO
     * @param previousStatus 변경 전 상태값
     * @param changedAt 변경 일시
     * @param reason 변경 사유
     */
    void recordBenefitChange(
            AccountDTO account, BenefitType previousStatus, LocalDateTime changedAt, String reason);

    List<AccountBenefitLogDTO> getBenefitLogs(Long accountId);

    int getTodayProcessedAccountCount(Status newStatus);
}
