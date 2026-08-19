package com.app.maria.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import com.app.maria.domain.statistics.dto.StatisticsFilterDTO;
import com.app.maria.domain.statistics.dto.request.StatisticsFilterRequestDTO;
import com.app.maria.domain.statistics.mapper.StatisticsMapper;
import com.app.maria.global.clock.service.BusinessClockService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 18, 12, 0);

    @Mock StatisticsMapper statisticsMapper;

    @Mock BusinessClockService businessClockService;

    @InjectMocks StatisticsServiceImpl statisticsService;

    @Test
    @DisplayName("keyword/productName은 그대로, referenceDate는 BusinessClockService의 오늘로 매퍼에 전달한다")
    void getAgeInvestmentStatsBuildsFilterWithPassthroughFieldsAndReferenceDateFromClock() {
        when(businessClockService.now()).thenReturn(NOW);
        when(statisticsMapper.selectAgeInvestmentStats(any())).thenReturn(List.of());
        StatisticsFilterRequestDTO request =
                StatisticsFilterRequestDTO.builder().keyword("홍길동").productName("삼성전자").build();

        statisticsService.getAgeInvestmentStats(request);

        ArgumentCaptor<StatisticsFilterDTO> captor =
                ArgumentCaptor.forClass(StatisticsFilterDTO.class);
        verify(statisticsMapper).selectAgeInvestmentStats(captor.capture());
        StatisticsFilterDTO filter = captor.getValue();
        assertThat(filter.getKeyword()).isEqualTo("홍길동");
        assertThat(filter.getProductName()).isEqualTo("삼성전자");
        assertThat(filter.getReferenceDate()).isEqualTo(LocalDate.of(2026, 8, 18));
    }

    @Test
    @DisplayName("startDate/endDate는 각각 그 날의 00:00:00과 23:59:59로 변환해서 매퍼에 전달한다")
    void getProductPurchaseStatsConvertsDateRangeToStartAndEndOfDay() {
        when(businessClockService.now()).thenReturn(NOW);
        when(statisticsMapper.selectProductPurchaseStats(any())).thenReturn(List.of());
        StatisticsFilterRequestDTO request =
                StatisticsFilterRequestDTO.builder()
                        .startDate(LocalDate.of(2026, 8, 1))
                        .endDate(LocalDate.of(2026, 8, 10))
                        .build();

        statisticsService.getProductPurchaseStats(request);

        ArgumentCaptor<StatisticsFilterDTO> captor =
                ArgumentCaptor.forClass(StatisticsFilterDTO.class);
        verify(statisticsMapper).selectProductPurchaseStats(captor.capture());
        StatisticsFilterDTO filter = captor.getValue();
        assertThat(filter.getStartDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0, 0));
        assertThat(filter.getEndDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 10, 23, 59, 59));
    }

    @Test
    @DisplayName("날짜 필터가 없으면 startDateTime/endDateTime은 null로 매퍼에 전달한다")
    void getFxExchangeStatsLeavesDateRangeNullWhenNotProvided() {
        when(businessClockService.now()).thenReturn(NOW);
        when(statisticsMapper.selectFxExchangeStats(any())).thenReturn(List.of());

        statisticsService.getFxExchangeStats(StatisticsFilterRequestDTO.builder().build());

        ArgumentCaptor<StatisticsFilterDTO> captor =
                ArgumentCaptor.forClass(StatisticsFilterDTO.class);
        verify(statisticsMapper).selectFxExchangeStats(captor.capture());
        assertThat(captor.getValue().getStartDateTime()).isNull();
        assertThat(captor.getValue().getEndDateTime()).isNull();
    }

    @Test
    @DisplayName("매퍼가 반환한 목록을 가공 없이 그대로 반환한다")
    void getAgeInvestmentStatsReturnsMapperResultAsIs() {
        when(businessClockService.now()).thenReturn(NOW);
        List<AgeInvestmentStatDTO> mapperResult =
                List.of(AgeInvestmentStatDTO.builder().ageGroup("30대").purchaseCount(3).build());
        when(statisticsMapper.selectAgeInvestmentStats(any())).thenReturn(mapperResult);

        List<AgeInvestmentStatDTO> result =
                statisticsService.getAgeInvestmentStats(
                        StatisticsFilterRequestDTO.builder().build());

        assertThat(result).isEqualTo(mapperResult);
    }

    @Test
    @DisplayName("결과가 없으면 빈 목록을 반환한다")
    void getAccountBenefitStatsReturnsEmptyListWhenMapperReturnsNothing() {
        when(businessClockService.now()).thenReturn(NOW);
        when(statisticsMapper.selectAccountBenefitStats(any())).thenReturn(List.of());

        List<?> result =
                statisticsService.getAccountBenefitStats(
                        StatisticsFilterRequestDTO.builder().build());

        assertThat(result).isEmpty();
    }
}
