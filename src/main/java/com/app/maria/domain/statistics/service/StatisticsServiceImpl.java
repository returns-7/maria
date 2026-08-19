package com.app.maria.domain.statistics.service;

import com.app.maria.domain.statistics.dto.AccountBenefitStatDTO;
import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import com.app.maria.domain.statistics.dto.FxExchangeStatDTO;
import com.app.maria.domain.statistics.dto.ProductPurchaseStatDTO;
import com.app.maria.domain.statistics.dto.ReliefRateStatDTO;
import com.app.maria.domain.statistics.dto.StatisticsFilterDTO;
import com.app.maria.domain.statistics.dto.request.StatisticsFilterRequestDTO;
import com.app.maria.domain.statistics.mapper.StatisticsMapper;
import com.app.maria.global.clock.service.BusinessClockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final BusinessClockService businessClockService;

    @Override
    public List<AgeInvestmentStatDTO> getAgeInvestmentStats(StatisticsFilterRequestDTO request) {
        return statisticsMapper.selectAgeInvestmentStats(buildFilter(request));
    }

    @Override
    public List<ProductPurchaseStatDTO> getProductPurchaseStats(
            StatisticsFilterRequestDTO request) {
        return statisticsMapper.selectProductPurchaseStats(buildFilter(request));
    }

    @Override
    public List<FxExchangeStatDTO> getFxExchangeStats(StatisticsFilterRequestDTO request) {
        return statisticsMapper.selectFxExchangeStats(buildFilter(request));
    }

    @Override
    public List<AccountBenefitStatDTO> getAccountBenefitStats(StatisticsFilterRequestDTO request) {
        return statisticsMapper.selectAccountBenefitStats(buildFilter(request));
    }

    @Override
    public List<ReliefRateStatDTO> getReliefRateStats(StatisticsFilterRequestDTO request) {
        return statisticsMapper.selectReliefRateStats(buildFilter(request));
    }

    private StatisticsFilterDTO buildFilter(StatisticsFilterRequestDTO request) {
        return request.toFilterDTO().toBuilder()
                .referenceDate(businessClockService.now().toLocalDate())
                .build();
    }
}
