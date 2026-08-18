package com.app.maria.domain.domestic.service;

import com.app.maria.domain.domestic.dto.DomesticAccountDetailDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentPageDTO;
import com.app.maria.domain.domestic.dto.request.DomesticInvestmentSearchRequestDTO;

public interface DomesticInvestmentService {
    DomesticInvestmentPageDTO getInvestments(DomesticInvestmentSearchRequestDTO request);

    DomesticAccountDetailDTO getAccountDetail(Long accountId);
}
