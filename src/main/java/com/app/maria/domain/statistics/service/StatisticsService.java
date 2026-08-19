package com.app.maria.domain.statistics.service;

import com.app.maria.domain.statistics.dto.AccountBenefitStatDTO;
import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import com.app.maria.domain.statistics.dto.FxExchangeStatDTO;
import com.app.maria.domain.statistics.dto.ProductPurchaseStatDTO;
import com.app.maria.domain.statistics.dto.ReliefRateStatDTO;
import com.app.maria.domain.statistics.dto.request.StatisticsFilterRequestDTO;
import java.util.List;

public interface StatisticsService {

    List<AgeInvestmentStatDTO> getAgeInvestmentStats(StatisticsFilterRequestDTO request);

    List<ProductPurchaseStatDTO> getProductPurchaseStats(StatisticsFilterRequestDTO request);

    List<FxExchangeStatDTO> getFxExchangeStats(StatisticsFilterRequestDTO request);

    List<AccountBenefitStatDTO> getAccountBenefitStats(StatisticsFilterRequestDTO request);

    List<ReliefRateStatDTO> getReliefRateStats(StatisticsFilterRequestDTO request);
}
