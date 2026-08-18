package com.app.maria.domain.accountclosure.service;

import com.app.maria.domain.accountclosure.dto.request.AccountClosureApplyRequestDTO;
import com.app.maria.domain.accountclosure.dto.response.AccountClosureDetailResponseDTO;
import com.app.maria.domain.accountclosure.dto.response.AccountClosureResponseDTO;
import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.util.List;

public interface AccountClosureService {
    Long applyClosure(Long customerId, AccountClosureApplyRequestDTO requestDTO);

    void rejectClosure(Long adminId, Long closureRequestId, String reason);

    void approveClosure(Long adminId, Long closureRequestId);

    List<AccountClosureResponseDTO> getClosures(AccountClosureStatus status);

    AccountClosureDetailResponseDTO getClosure(Long closureRequestId);
}
