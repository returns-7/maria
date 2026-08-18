package com.app.maria.domain.domestic.service;

import com.app.maria.domain.account.exception.AccountNotFoundException;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.domestic.dto.*;
import com.app.maria.domain.domestic.dto.request.DomesticInvestmentSearchRequestDTO;
import com.app.maria.domain.domestic.dto.request.DomesticTradeRequestDTO;
import com.app.maria.domain.domestic.exception.DomesticInvestmentNotFoundException;
import com.app.maria.domain.domestic.mapper.DomesticStockBalanceMapper;
import com.app.maria.global.response.ApiResponseDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class, readOnly = true)
public class DomesticInvestmentServiceImpl implements DomesticInvestmentService {

    private final DomesticStockBalanceMapper domesticStockBalanceMapper;
    private final AccountMapper accountMapper;
    private final DomesticPurchaseEligibilityService domesticPurchaseEligibilityService;
    private final RestClient restClient;

    public DomesticInvestmentServiceImpl(
            DomesticStockBalanceMapper domesticStockBalanceMapper,
            AccountMapper accountMapper,
            DomesticPurchaseEligibilityService domesticPurchaseEligibilityService,
            @Qualifier("returnSecuritiesRestClient") RestClient restClient) {
        this.domesticStockBalanceMapper = domesticStockBalanceMapper;
        this.accountMapper = accountMapper;
        this.domesticPurchaseEligibilityService = domesticPurchaseEligibilityService;
        this.restClient = restClient;
    }

    @Override
    public DomesticInvestmentPageDTO getInvestments(DomesticInvestmentSearchRequestDTO request) {
        var condition = request.toDomesticInvestmentSearchDTO();
        List<DomesticInvestmentListDTO> content =
                domesticStockBalanceMapper.selectAccountSummaries(condition);
        int totalElements = domesticStockBalanceMapper.countAccountSummaries(condition);
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        return DomesticInvestmentPageDTO.builder()
                .content(content)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public DomesticAccountDetailDTO getAccountDetail(Long accountId) {
        DomesticInvestmentListDTO summary =
                domesticStockBalanceMapper
                        .selectAccountSummaryById(accountId)
                        .orElseThrow(
                                () -> new DomesticInvestmentNotFoundException("계좌를 찾을 수 없습니다."));

        List<DomesticHoldingDTO> holdings =
                domesticStockBalanceMapper.selectHoldingsByAccountId(accountId);
        holdings.forEach(
                h ->
                        h.setCurrentlyPurchasable(
                                domesticPurchaseEligibilityService.isPurchasable(
                                        h.getType(),
                                        h.getDomesticStockRatio(),
                                        h.getInceptionDate())));

        Long customerId =
                accountMapper
                        .selectByAccountId(accountId)
                        .orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."))
                        .getCustomerId();
        String ciHash =
                accountMapper
                        .selectCiHashByCustomerId(customerId)
                        .orElseThrow(() -> new AccountNotFoundException("고객 식별정보를 찾을 수 없습니다."));

        List<DomesticTradeHistoryDTO> tradeHistory = fetchDomesticTradeHistory(ciHash);

        return DomesticAccountDetailDTO.builder()
                .accountId(summary.getAccountId())
                .accountNo(summary.getAccountNo())
                .customerName(summary.getCustomerName())
                .cashAmount(summary.getCashAmount())
                .holdings(holdings)
                .tradeHistory(tradeHistory)
                .build();
    }

    private List<DomesticTradeHistoryDTO> fetchDomesticTradeHistory(String ciHash) {
        try {
            ApiResponseDTO<List<DomesticTradeHistoryDTO>> apiResponse =
                    restClient
                            .post()
                            .uri("/api/domestic-trades")
                            .body(new DomesticTradeRequestDTO(ciHash))
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            ApiResponseDTO<List<DomesticTradeHistoryDTO>>>() {});
            if (apiResponse == null || apiResponse.getData() == null) {
                return List.of();
            }
            return apiResponse.getData();
        } catch (RestClientException e) {
            log.warn("증권사 매매내역 조회 실패 - 매매내역 없이 나머지 정보만 반환합니다.", e);
            return List.of();
        }
    }
}
