package com.app.maria.domain.domestic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.exception.AccountNotFoundException;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.domestic.dto.DomesticAccountDetailDTO;
import com.app.maria.domain.domestic.dto.DomesticHoldingDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentListDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentPageDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentSearchDTO;
import com.app.maria.domain.domestic.dto.DomesticTradeHistoryDTO;
import com.app.maria.domain.domestic.dto.request.DomesticInvestmentSearchRequestDTO;
import com.app.maria.domain.domestic.dto.request.DomesticTradeRequestDTO;
import com.app.maria.domain.domestic.exception.DomesticInvestmentNotFoundException;
import com.app.maria.domain.domestic.mapper.DomesticStockBalanceMapper;
import com.app.maria.global.response.ApiResponseDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class DomesticInvestmentServiceImplTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long CUSTOMER_ID = 5177L;
    private static final String CI_HASH = "ci-hash-5177";

    @Mock private DomesticStockBalanceMapper domesticStockBalanceMapper;

    @Mock private AccountMapper accountMapper;

    @Mock private DomesticPurchaseEligibilityService domesticPurchaseEligibilityService;

    @Mock private RestClient restClient;

    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock private RestClient.RequestBodySpec requestBodySpec;

    @Mock private RestClient.ResponseSpec responseSpec;

    private DomesticInvestmentServiceImpl domesticInvestmentService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        domesticInvestmentService =
                new DomesticInvestmentServiceImpl(
                        domesticStockBalanceMapper,
                        accountMapper,
                        domesticPurchaseEligibilityService,
                        restClient);

        lenient()
                .when(accountMapper.selectByAccountId(ACCOUNT_ID))
                .thenReturn(
                        Optional.of(
                                AccountDTO.builder()
                                        .accountId(ACCOUNT_ID)
                                        .customerId(CUSTOMER_ID)
                                        .build()));
        lenient()
                .when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID))
                .thenReturn(Optional.of(CI_HASH));
        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient()
                .when(requestBodyUriSpec.uri(eq("/api/domestic-trades")))
                .thenReturn(requestBodySpec);
        lenient()
                .when(requestBodySpec.body(any(DomesticTradeRequestDTO.class)))
                .thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("getInvestments()는 totalElements를 size로 나눈 뒤 올림해 totalPages를 계산한다")
    void getInvestmentsCalculatesTotalPagesFromTotalElements() {
        when(domesticStockBalanceMapper.selectAccountSummaries(any())).thenReturn(List.of());
        when(domesticStockBalanceMapper.countAccountSummaries(any())).thenReturn(45);

        DomesticInvestmentSearchRequestDTO request =
                DomesticInvestmentSearchRequestDTO.builder().page(0).size(20).build();

        DomesticInvestmentPageDTO result = domesticInvestmentService.getInvestments(request);

        assertThat(result.getTotalElements()).isEqualTo(45);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("getInvestments()는 검색 조건(고객명/페이지/사이즈)을 mapper에 그대로 전달한다")
    void getInvestmentsPassesSearchConditionToMapper() {
        when(domesticStockBalanceMapper.selectAccountSummaries(any())).thenReturn(List.of());
        when(domesticStockBalanceMapper.countAccountSummaries(any())).thenReturn(0);

        DomesticInvestmentSearchRequestDTO request =
                DomesticInvestmentSearchRequestDTO.builder()
                        .customerName("홍길동")
                        .page(2)
                        .size(10)
                        .build();

        domesticInvestmentService.getInvestments(request);

        var captor = org.mockito.ArgumentCaptor.forClass(DomesticInvestmentSearchDTO.class);
        verify(domesticStockBalanceMapper).selectAccountSummaries(captor.capture());
        assertThat(captor.getValue().getCustomerName()).isEqualTo("홍길동");
        assertThat(captor.getValue().getOffset()).isEqualTo(20);
        assertThat(captor.getValue().getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("getAccountDetail()은 예탁금·보유종목·매매내역을 조합해서 반환한다")
    @SuppressWarnings("unchecked")
    void getAccountDetailReturnsSummaryHoldingsAndTradeHistory() {
        when(domesticStockBalanceMapper.selectAccountSummaryById(ACCOUNT_ID))
                .thenReturn(
                        Optional.of(
                                DomesticInvestmentListDTO.builder()
                                        .accountId(ACCOUNT_ID)
                                        .accountNo("1234567890")
                                        .customerName("홍길동")
                                        .cashAmount(BigDecimal.valueOf(500000))
                                        .build()));
        DomesticHoldingDTO holding =
                DomesticHoldingDTO.builder().domesticProductId(1L).ticker("005930").build();
        when(domesticStockBalanceMapper.selectHoldingsByAccountId(ACCOUNT_ID))
                .thenReturn(List.of(holding));
        when(domesticPurchaseEligibilityService.isPurchasable(any(), any(), any()))
                .thenReturn(true);

        DomesticTradeHistoryDTO trade =
                DomesticTradeHistoryDTO.builder().stockCode("005930").tradeType("BUY").build();
        ApiResponseDTO<List<DomesticTradeHistoryDTO>> apiResponse =
                ApiResponseDTO.of("조회 성공", List.of(trade));
        when(responseSpec.body(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(apiResponse);

        DomesticAccountDetailDTO result = domesticInvestmentService.getAccountDetail(ACCOUNT_ID);

        assertThat(result.getAccountNo()).isEqualTo("1234567890");
        assertThat(result.getCustomerName()).isEqualTo("홍길동");
        assertThat(result.getHoldings()).hasSize(1);
        assertThat(result.getHoldings().get(0).getCurrentlyPurchasable()).isTrue();
        assertThat(result.getTradeHistory()).hasSize(1);
        assertThat(result.getTradeHistory().get(0).getStockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("계좌 요약이 없으면 DomesticInvestmentNotFoundException을 던진다")
    void getAccountDetailThrowsWhenAccountSummaryNotFound() {
        when(domesticStockBalanceMapper.selectAccountSummaryById(ACCOUNT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> domesticInvestmentService.getAccountDetail(ACCOUNT_ID))
                .isInstanceOf(DomesticInvestmentNotFoundException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("고객의 ciHash를 찾을 수 없으면 AccountNotFoundException을 던진다")
    void getAccountDetailThrowsWhenCiHashMissing() {
        when(domesticStockBalanceMapper.selectAccountSummaryById(ACCOUNT_ID))
                .thenReturn(
                        Optional.of(
                                DomesticInvestmentListDTO.builder().accountId(ACCOUNT_ID).build()));
        when(domesticStockBalanceMapper.selectHoldingsByAccountId(ACCOUNT_ID))
                .thenReturn(List.of());
        when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domesticInvestmentService.getAccountDetail(ACCOUNT_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("고객 식별정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("증권사 응답 data가 null이면 매매내역을 빈 리스트로 반환한다")
    @SuppressWarnings("unchecked")
    void getAccountDetailReturnsEmptyTradeHistoryWhenApiResponseDataIsNull() {
        when(domesticStockBalanceMapper.selectAccountSummaryById(ACCOUNT_ID))
                .thenReturn(
                        Optional.of(
                                DomesticInvestmentListDTO.builder().accountId(ACCOUNT_ID).build()));
        when(domesticStockBalanceMapper.selectHoldingsByAccountId(ACCOUNT_ID))
                .thenReturn(List.of());
        ApiResponseDTO<List<DomesticTradeHistoryDTO>> apiResponse =
                ApiResponseDTO.of("조회 실패", null);
        when(responseSpec.body(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(apiResponse);

        DomesticAccountDetailDTO result = domesticInvestmentService.getAccountDetail(ACCOUNT_ID);

        assertThat(result.getTradeHistory()).isEmpty();
    }

    @Test
    @DisplayName("증권사 시스템 연결 실패 시 매매내역을 빈 리스트로 반환한다")
    @SuppressWarnings("unchecked")
    void getAccountDetailReturnsEmptyTradeHistoryWhenReturnSecuritiesCallFails() {
        when(domesticStockBalanceMapper.selectAccountSummaryById(ACCOUNT_ID))
                .thenReturn(
                        Optional.of(
                                DomesticInvestmentListDTO.builder().accountId(ACCOUNT_ID).build()));
        when(domesticStockBalanceMapper.selectHoldingsByAccountId(ACCOUNT_ID))
                .thenReturn(List.of());
        when(responseSpec.body(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("증권사 시스템 연결 실패"));

        DomesticAccountDetailDTO result = domesticInvestmentService.getAccountDetail(ACCOUNT_ID);

        assertThat(result.getTradeHistory()).isEmpty();
    }
}
