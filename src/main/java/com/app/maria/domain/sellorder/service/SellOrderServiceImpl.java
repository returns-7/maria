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
import com.app.maria.global.audit.dto.AuditLogDTO;
import com.app.maria.global.audit.service.AuditLogService;
import com.app.maria.global.client.exchange.ExchangeRateClient;
import com.app.maria.global.client.kis.KisExchangeCode;
import com.app.maria.global.client.kis.KisPriceClient;
import com.app.maria.global.clock.service.BusinessClockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SellOrderServiceImpl implements SellOrderService{

    private final SellOrderMapper sellOrderMapper;
    private final KisPriceClient kis;
    private final ExchangeRateClient exchange;
    private final InboundMapper inboundMapper;
    private final ForeignProductMapper foreignProductMapper;
    private final SellLimitService sellLimitService;
    private final BusinessClockService businessClockService;
    private final ProvisionalExchangeService provisionalExchangeService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<SellOrderResponseDTO> placeSellOrder(Long actorAdminId,SellOrderRequestDTO request) {

        SellOrderDTO sellOrderDTO = request.toSellOrderDTO();

        List<InboundDetailDTO> lots = inboundMapper.selectFifoLots(sellOrderDTO.getAccountId(), sellOrderDTO.getForeignProductId());

        BigDecimal totalCurrentQty = lots.stream()
                .map(InboundDetailDTO::getCurrentQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sellOrderDTO.getSellQty().compareTo(totalCurrentQty) > 0) {
            throw new SellOrderException("매도 가능 수량을 초과했습니다.");
        }

        ForeignProductDTO product = foreignProductMapper.selectById(sellOrderDTO.getForeignProductId())
                .orElseThrow(() -> new ForeignProductNotFoundException("종목 정보를 찾을 수 없습니다."));

        String kisMarketCode = KisExchangeCode.fromMarket(product.getMarket());
        BigDecimal previousClose = kis.getPreviousClose(kisMarketCode, product.getTicker());

        BigDecimal exchangeRate = exchange.getBaseRate(product.getCurrency());
        sellOrderDTO.setBasePrice(previousClose.multiply(exchangeRate));
        sellOrderDTO.setSettlementFxRate(exchangeRate);

        BigDecimal orderAmount = sellOrderDTO.getSellQty().multiply(sellOrderDTO.getBasePrice());

        boolean withinLimit = sellLimitService.isWithinSellLimit(sellOrderDTO.getAccountId(), orderAmount);

        if (!withinLimit) {
            SellOrderDTO rejected = sellOrderDTO.toBuilder()
                    .inboundDetailId(null)
                    .status(SellOrderStatus.REJECTED)
                    .processedAt(businessClockService.now())
                    .build();
            sellOrderMapper.insertSellOrder(rejected);

            auditLogService.log(AuditLogDTO.builder()
                    .adminId(actorAdminId)
                    .targetTable("SELL_ORDER")
                    .targetPk(String.valueOf(rejected.getOrderId()))
                    .beforeValue(null)
                    .afterValue("REJECTED / accountId=" + rejected.getAccountId()
                            + ", foreignProductId=" + rejected.getForeignProductId()
                            + ", sellQty=" + rejected.getSellQty())
                    .reasonCode("SELL_ORDER_REJECTED")
                    .build());

            return List.of(new SellOrderResponseDTO(rejected));
        }

        BigDecimal remainingQty = sellOrderDTO.getSellQty();
        List<SellOrderDTO> executeOrders = new ArrayList<>();

        for (InboundDetailDTO lot: lots) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal qtyFromThisLot = remainingQty.min(lot.getCurrentQty());

            int updateRows = inboundMapper.decreaseCurrentQty(lot.getInboundDetailId(), qtyFromThisLot);
            if (updateRows == 0) {
                throw new SellOrderException("다른 요청이 먼저 처리되었습니다.");
            }

            SellOrderDTO executed = sellOrderDTO.toBuilder()
                    .inboundDetailId(lot.getInboundDetailId())
                    .sellQty(qtyFromThisLot)
                    .status(SellOrderStatus.EXECUTED)
                    .processedAt(businessClockService.now())
                    .build();
            sellOrderMapper.insertSellOrder(executed);
            executeOrders.add(executed);

            auditLogService.log(AuditLogDTO.builder()
                    .adminId(actorAdminId)
                    .targetTable("SELL_ORDER")
                    .targetPk(String.valueOf(executed.getOrderId()))
                    .beforeValue("inboundDetailId=" + lot.getInboundDetailId() + ", lotCurrentQty=" + lot.getCurrentQty())
                    .afterValue("EXECUTED / sellQty=" + qtyFromThisLot)
                    .reasonCode("SELL_ORDER_EXECUTED")
                    .build());

            provisionalExchangeService.createProvisionalExchange(executed);

            remainingQty = remainingQty.subtract(qtyFromThisLot);

        }

        return  executeOrders.stream().map(SellOrderResponseDTO::new).toList();

    }

    @Override
    @Transactional(readOnly = true)
    public SellOrderResponseDTO getSellOrder(Long orderId) {
        SellOrderDTO dto =  sellOrderMapper.selectSellOrderById(orderId).orElseThrow(() -> new SellOrderNotFoundException("매도 주문 조회 실패"));
        return new SellOrderResponseDTO(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellOrderResponseDTO> getSellOrderByAccount(Long accountId) {
        List<SellOrderDTO> orders = sellOrderMapper.selectSellOrdersByAccountId(accountId);
        return orders.stream()
                .map(SellOrderResponseDTO::new)
                .toList();
    }

}
