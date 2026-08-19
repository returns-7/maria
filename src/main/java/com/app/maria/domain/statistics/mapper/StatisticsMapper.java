package com.app.maria.domain.statistics.mapper;

import com.app.maria.domain.statistics.dto.AccountBenefitStatDTO;
import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import com.app.maria.domain.statistics.dto.FxExchangeStatDTO;
import com.app.maria.domain.statistics.dto.ProductPurchaseStatDTO;
import com.app.maria.domain.statistics.dto.ReliefRateStatDTO;
import com.app.maria.domain.statistics.dto.StatisticsFilterDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatisticsMapper {

    List<AgeInvestmentStatDTO> selectAgeInvestmentStats(StatisticsFilterDTO filter);

    List<ProductPurchaseStatDTO> selectProductPurchaseStats(StatisticsFilterDTO filter);

    List<FxExchangeStatDTO> selectFxExchangeStats(StatisticsFilterDTO filter);

    List<AccountBenefitStatDTO> selectAccountBenefitStats(StatisticsFilterDTO filter);

    List<ReliefRateStatDTO> selectReliefRateStats(StatisticsFilterDTO filter);
}
