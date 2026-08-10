package com.app.maria.domain.sellorder.service;

import com.app.maria.domain.sellorder.exception.SellOrderException;
import com.app.maria.domain.sellorder.mapper.SellLimitMapper;
import com.app.maria.global.client.mydata.MydataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SellLimitServiceImpl implements SellLimitService {

    private static final BigDecimal MAX_TOTAL_SELL_LIMIT = BigDecimal.valueOf(50_000_000);

    private final SellLimitMapper sellLimitMapper;
    private final MydataClient mydataClient;

    @Override
    public boolean isWithinSellLimit(Long accountId, BigDecimal orderAmount) {
        BigDecimal limitAmount = sellLimitMapper.selectAccountLimitForUpdate(accountId)
                .orElseThrow(() -> new SellOrderException("계좌 한도 정보를 찾을 수 없습니다."));

        BigDecimal finalizedSum = sellLimitMapper.sumFinalizedExchangeAmount(accountId);

        BigDecimal pendingSum = sellLimitMapper.sumPendingSellOrderAmount(accountId);

        String ciHash = sellLimitMapper.selectCiHashByAccountId(accountId)
                .orElseThrow(() ->  new SellOrderException("고객 정보를 확인할 수 없습니다."));

        BigDecimal externalSum = mydataClient.getExternalSellTotal(ciHash);

        BigDecimal localTotal = finalizedSum.add(pendingSum).add(orderAmount);
        BigDecimal globalTotal = localTotal.add(externalSum);

        boolean withinAccountLimit = localTotal.compareTo(limitAmount) <= 0;
        boolean withinGlobalCap = globalTotal.compareTo(MAX_TOTAL_SELL_LIMIT) <= 0;

        return withinAccountLimit && withinGlobalCap;
    }

}
