package com.app.maria.domain.sellorder.service;

import com.app.maria.domain.foreignproduct.dto.ForeignProductDTO;
import com.app.maria.domain.foreignproduct.exception.ForeignProductNotFoundException;
import com.app.maria.domain.foreignproduct.mapper.ForeignProductMapper;
import com.app.maria.domain.inbound.dto.InboundDetailDTO;
import com.app.maria.domain.inbound.mapper.InboundMapper;
import com.app.maria.domain.sellorder.dto.SellOrderDTO;
import com.app.maria.domain.sellorder.dto.request.SellOrderRequestDTO;
import com.app.maria.domain.sellorder.dto.response.SellOrderResponseDTO;
import com.app.maria.domain.sellorder.exception.SellOrderException;
import com.app.maria.domain.sellorder.exception.SellOrderNotFoundException;
import com.app.maria.domain.sellorder.mapper.SellOrderMapper;
import com.app.maria.domain.sellorder.type.SellOrderStatus;
import com.app.maria.domain.settlement.service.ProvisionalExchangeService;
import com.app.maria.global.client.exchange.ExchangeRateClient;
import com.app.maria.global.client.kis.KisPriceClient;
import com.app.maria.global.clock.service.BusinessClockService;
import com.app.maria.global.exception.KisPriceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellOrderServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 9, 12, 0);

    @Mock
    SellOrderMapper sellOrderMapper;

    @Mock
    KisPriceClient kisPriceClient;

    @Mock
    ExchangeRateClient exchangeRateClient;

    @Mock
    InboundMapper inboundMapper;

    @Mock
    ForeignProductMapper foreignProductMapper;

    @Mock
    SellLimitService sellLimitService;

    @Mock
    BusinessClockService businessClockService;

    @Mock
    ProvisionalExchangeService provisionalExchangeService;

    @InjectMocks
    SellOrderServiceImpl sellOrderService;

    private SellOrderRequestDTO.SellOrderRequestDTOBuilder validRequestBuilder() {
        return SellOrderRequestDTO.builder()
                .accountId(1L)
                .foreignProductId(10L)
                .sellQty(new BigDecimal("10"));
    }

    private InboundDetailDTO lot(Long inboundDetailId, String currentQty, LocalDateTime purchaseDate) {
        return InboundDetailDTO.builder()
                .inboundDetailId(inboundDetailId)
                .foreignProductId(10L)
                .currentQty(new BigDecimal(currentQty))
                .purchaseDate(purchaseDate)
                .build();
    }

    private ForeignProductDTO validProduct() {
        return ForeignProductDTO.builder()
                .foreignProductId(10L)
                .market("NASDAQ")
                .ticker("AAPL")
                .currency("USD")
                .build();
    }

    private void stubPriceAndRate() {
        when(kisPriceClient.getPreviousClose("NAS", "AAPL")).thenReturn(new BigDecimal("308.91"));
        when(exchangeRateClient.getBaseRate("USD")).thenReturn(new BigDecimal("1433.6"));
    }

    @Test
    @DisplayName("한도 이내면서 lot 하나로 전량 체결되면 잔량을 차감하고 전일종가와 환율을 곱해 EXECUTED 상태 1건으로 저장한다")
    void placeSellOrderDecreasesQtyAndSavesAsExecutedWhenSingleLotCoversFullQty() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        when(inboundMapper.selectFifoLots(1L, 10L))
                .thenReturn(List.of(lot(1L, "50", LocalDateTime.of(2026, 1, 1, 0, 0))));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(true);
        when(inboundMapper.decreaseCurrentQty(1L, new BigDecimal("10"))).thenReturn(1);
        when(businessClockService.now()).thenReturn(NOW);
        BigDecimal expectedBasePrice = new BigDecimal("308.91").multiply(new BigDecimal("1433.6"));

        List<SellOrderResponseDTO> result = sellOrderService.placeSellOrder(request);

        assertThat(result).hasSize(1);
        SellOrderResponseDTO first = result.get(0);
        assertThat(first.getInboundDetailId()).isEqualTo(1L);
        assertThat(first.getSellQty()).isEqualByComparingTo("10");
        assertThat(first.getStatus()).isEqualTo(SellOrderStatus.EXECUTED);
        assertThat(first.getBasePrice()).isEqualByComparingTo(expectedBasePrice);
        assertThat(first.getSettlementFxRate()).isEqualByComparingTo("1433.6");
        assertThat(first.getProcessedAt()).isEqualTo(NOW);

        verify(inboundMapper, times(1)).decreaseCurrentQty(1L, new BigDecimal("10"));
        verify(provisionalExchangeService, times(1)).createProvisionalExchange(any(SellOrderDTO.class));

        ArgumentCaptor<SellOrderDTO> captor = ArgumentCaptor.forClass(SellOrderDTO.class);
        verify(sellOrderMapper, times(1)).insertSellOrder(captor.capture());
        SellOrderDTO saved = captor.getValue();
        assertThat(saved.getInboundDetailId()).isEqualTo(1L);
        assertThat(saved.getSellQty()).isEqualByComparingTo("10");
        assertThat(saved.getStatus()).isEqualTo(SellOrderStatus.EXECUTED);
        assertThat(saved.getBasePrice()).isEqualByComparingTo(expectedBasePrice);
        assertThat(saved.getSettlementFxRate()).isEqualByComparingTo("1433.6");
        assertThat(saved.getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("한 lot의 잔량으로 부족하면 오래된 lot부터 순서대로 나눠 체결하고 lot마다 매도 주문을 하나씩 저장한다")
    void placeSellOrderSplitsAcrossMultipleLotsInFifoOrderWhenSingleLotIsInsufficient() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        InboundDetailDTO olderLot = lot(1L, "6", LocalDateTime.of(2026, 1, 1, 0, 0));
        InboundDetailDTO newerLot = lot(2L, "50", LocalDateTime.of(2026, 2, 1, 0, 0));
        when(inboundMapper.selectFifoLots(1L, 10L)).thenReturn(List.of(olderLot, newerLot));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(true);
        when(inboundMapper.decreaseCurrentQty(1L, new BigDecimal("6"))).thenReturn(1);
        when(inboundMapper.decreaseCurrentQty(2L, new BigDecimal("4"))).thenReturn(1);
        when(businessClockService.now()).thenReturn(NOW);

        List<SellOrderResponseDTO> result = sellOrderService.placeSellOrder(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getInboundDetailId()).isEqualTo(1L);
        assertThat(result.get(0).getSellQty()).isEqualByComparingTo("6");
        assertThat(result.get(1).getInboundDetailId()).isEqualTo(2L);
        assertThat(result.get(1).getSellQty()).isEqualByComparingTo("4");
        assertThat(result).allSatisfy(r -> assertThat(r.getStatus()).isEqualTo(SellOrderStatus.EXECUTED));

        verify(inboundMapper, times(1)).decreaseCurrentQty(1L, new BigDecimal("6"));
        verify(inboundMapper, times(1)).decreaseCurrentQty(2L, new BigDecimal("4"));
        verify(sellOrderMapper, times(2)).insertSellOrder(any());
        verify(provisionalExchangeService, times(2)).createProvisionalExchange(any(SellOrderDTO.class));
    }

    @Test
    @DisplayName("정상 요청이면 sellLimitService에 계좌ID와 매도금액(원요청 수량x기준가) 전체를 정확히 한 번만 넘겨 한도를 검증한다")
    void placeSellOrderCallsSellLimitServiceOnceWithAccountIdAndTotalOrderAmount() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        InboundDetailDTO olderLot = lot(1L, "6", LocalDateTime.of(2026, 1, 1, 0, 0));
        InboundDetailDTO newerLot = lot(2L, "50", LocalDateTime.of(2026, 2, 1, 0, 0));
        when(inboundMapper.selectFifoLots(1L, 10L)).thenReturn(List.of(olderLot, newerLot));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(true);
        when(inboundMapper.decreaseCurrentQty(any(), any())).thenReturn(1);
        when(businessClockService.now()).thenReturn(NOW);
        BigDecimal expectedOrderAmount = new BigDecimal("10")
                .multiply(new BigDecimal("308.91").multiply(new BigDecimal("1433.6")));

        sellOrderService.placeSellOrder(request);

        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(sellLimitService, times(1)).isWithinSellLimit(eq(1L), amountCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(expectedOrderAmount);
    }

    @Test
    @DisplayName("한도초과로 확인되면 lot 차감 없이 inboundDetailId가 없는 REJECTED 단건으로 저장한다 (basePrice/fxRate는 그대로 기록)")
    void placeSellOrderSavesAsRejectedWithoutDecreasingAnyLotWhenSellLimitExceeded() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        InboundDetailDTO olderLot = lot(1L, "6", LocalDateTime.of(2026, 1, 1, 0, 0));
        InboundDetailDTO newerLot = lot(2L, "50", LocalDateTime.of(2026, 2, 1, 0, 0));
        when(inboundMapper.selectFifoLots(1L, 10L)).thenReturn(List.of(olderLot, newerLot));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(false);
        when(businessClockService.now()).thenReturn(NOW);
        BigDecimal expectedBasePrice = new BigDecimal("308.91").multiply(new BigDecimal("1433.6"));

        List<SellOrderResponseDTO> result = sellOrderService.placeSellOrder(request);

        assertThat(result).hasSize(1);
        SellOrderResponseDTO rejected = result.get(0);
        assertThat(rejected.getInboundDetailId()).isNull();
        assertThat(rejected.getStatus()).isEqualTo(SellOrderStatus.REJECTED);
        assertThat(rejected.getProcessedAt()).isEqualTo(NOW);
        assertThat(rejected.getBasePrice()).isEqualByComparingTo(expectedBasePrice);
        assertThat(rejected.getSettlementFxRate()).isEqualByComparingTo("1433.6");

        verify(inboundMapper, never()).decreaseCurrentQty(any(), any());

        ArgumentCaptor<SellOrderDTO> captor = ArgumentCaptor.forClass(SellOrderDTO.class);
        verify(sellOrderMapper, times(1)).insertSellOrder(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SellOrderStatus.REJECTED);
        assertThat(captor.getValue().getInboundDetailId()).isNull();
    }

    @Test
    @DisplayName("전일종가 조회에 실패하면 예외가 전파되고 한도판정/저장/차감은 하지 않는다")
    void placeSellOrderPropagatesExceptionAndSkipsSaveWhenPreviousCloseLookupFails() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        when(inboundMapper.selectFifoLots(1L, 10L))
                .thenReturn(List.of(lot(1L, "50", LocalDateTime.of(2026, 1, 1, 0, 0))));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        when(kisPriceClient.getPreviousClose("NAS", "AAPL"))
                .thenThrow(new KisPriceNotFoundException("전일종가 조회 실패: AAPL"));

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(KisPriceNotFoundException.class);

        verify(exchangeRateClient, never()).getBaseRate(any());
        verifyNoInteractions(sellOrderMapper, sellLimitService);
        verify(inboundMapper, never()).decreaseCurrentQty(any(), any());
    }

    @Test
    @DisplayName("보유 중인 lot이 하나도 없으면 매도 가능 수량 초과로 예외를 던지고 종목 조회/외부 API/저장은 하지 않는다")
    void placeSellOrderThrowsWhenNoLotsExist() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        when(inboundMapper.selectFifoLots(1L, 10L)).thenReturn(List.of());

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(SellOrderException.class)
                .hasMessage("매도 가능 수량을 초과했습니다.");

        verify(inboundMapper, never()).decreaseCurrentQty(any(), any());
        verifyNoInteractions(kisPriceClient, exchangeRateClient, sellOrderMapper, foreignProductMapper, sellLimitService);
    }

    @Test
    @DisplayName("매도 수량이 보유 lot들의 currentQty 합계를 초과하면 예외를 던지고 종목 조회/외부 API/저장은 하지 않는다")
    void placeSellOrderThrowsWhenSellQtyExceedsTotalCurrentQtyAcrossLots() {
        SellOrderRequestDTO request = validRequestBuilder().sellQty(new BigDecimal("100")).build();
        when(inboundMapper.selectFifoLots(1L, 10L))
                .thenReturn(List.of(lot(1L, "50", LocalDateTime.of(2026, 1, 1, 0, 0))));

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(SellOrderException.class)
                .hasMessage("매도 가능 수량을 초과했습니다.");

        verify(inboundMapper, never()).decreaseCurrentQty(any(), any());
        verifyNoInteractions(kisPriceClient, exchangeRateClient, sellOrderMapper, foreignProductMapper, sellLimitService);
    }

    @Test
    @DisplayName("종목 정보를 찾을 수 없으면 예외를 던지고 잔량 차감/외부 API/저장은 하지 않는다")
    void placeSellOrderThrowsWhenForeignProductNotFound() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        when(inboundMapper.selectFifoLots(1L, 10L))
                .thenReturn(List.of(lot(1L, "50", LocalDateTime.of(2026, 1, 1, 0, 0))));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(ForeignProductNotFoundException.class)
                .hasMessage("종목 정보를 찾을 수 없습니다.");

        verify(inboundMapper, never()).decreaseCurrentQty(any(), any());
        verifyNoInteractions(kisPriceClient, exchangeRateClient, sellOrderMapper, sellLimitService);
    }

    @Test
    @DisplayName("한도 이내로 확인됐는데 첫 lot에서 동시성 충돌로 차감된 row가 없으면 예외를 던지고 그 lot은 저장하지 않는다")
    void placeSellOrderThrowsWhenDecreaseCurrentQtyAffectsNoRowsOnFirstLot() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        when(inboundMapper.selectFifoLots(1L, 10L))
                .thenReturn(List.of(lot(1L, "50", LocalDateTime.of(2026, 1, 1, 0, 0))));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(true);
        when(inboundMapper.decreaseCurrentQty(1L, new BigDecimal("10"))).thenReturn(0);

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(SellOrderException.class)
                .hasMessage("다른 요청이 먼저 처리되었습니다.");

        verifyNoInteractions(sellOrderMapper);
    }

    @Test
    @DisplayName("두 번째 lot 처리 중 동시성 충돌이 나면 첫 번째 lot은 이미 저장된 채로 예외가 전파된다 (트랜잭션 롤백은 @Transactional이 담당)")
    void placeSellOrderPropagatesExceptionAfterFirstLotAlreadySavedWhenSecondLotConflicts() {
        SellOrderRequestDTO request = validRequestBuilder().build();

        InboundDetailDTO olderLot = lot(1L, "6", LocalDateTime.of(2026, 1, 1, 0, 0));
        InboundDetailDTO newerLot = lot(2L, "50", LocalDateTime.of(2026, 2, 1, 0, 0));
        when(inboundMapper.selectFifoLots(1L, 10L)).thenReturn(List.of(olderLot, newerLot));
        when(foreignProductMapper.selectById(10L)).thenReturn(Optional.of(validProduct()));
        stubPriceAndRate();
        when(sellLimitService.isWithinSellLimit(any(), any())).thenReturn(true);
        when(inboundMapper.decreaseCurrentQty(1L, new BigDecimal("6"))).thenReturn(1);
        when(inboundMapper.decreaseCurrentQty(2L, new BigDecimal("4"))).thenReturn(0);
        when(businessClockService.now()).thenReturn(NOW);

        assertThatThrownBy(() -> sellOrderService.placeSellOrder(request))
                .isInstanceOf(SellOrderException.class)
                .hasMessage("다른 요청이 먼저 처리되었습니다.");

        verify(sellOrderMapper, times(1)).insertSellOrder(any());
    }

    @Test
    @DisplayName("존재하면 조회 결과를 반환한다")
    void getSellOrderReturnsResultWhenExists() {
        SellOrderDTO saved = SellOrderDTO.builder()
                .orderId(100L)
                .inboundDetailId(1L)
                .sellQty(new BigDecimal("10"))
                .status(SellOrderStatus.EXECUTED)
                .build();
        when(sellOrderMapper.selectSellOrderById(100L)).thenReturn(Optional.of(saved));

        SellOrderResponseDTO result = sellOrderService.getSellOrder(100L);

        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getInboundDetailId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(SellOrderStatus.EXECUTED);
    }

    @Test
    @DisplayName("존재하지 않으면 SellOrderNotFoundException을 던진다")
    void getSellOrderThrowsSellOrderNotFoundExceptionWhenNotFound() {
        when(sellOrderMapper.selectSellOrderById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellOrderService.getSellOrder(999L))
                .isInstanceOf(SellOrderNotFoundException.class);
    }
}
