package com.app.maria.domain.settlement.batch;

import com.app.maria.domain.settlement.component.SettlementFailureRecorder;
import com.app.maria.domain.settlement.component.SettlementBatchStatusUpdater;
import com.app.maria.domain.settlement.component.SettlementTransactionExecutor;
import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.dto.SettlementItemDTO;
import com.app.maria.domain.settlement.dto.SettlementJoinDTO;
import com.app.maria.domain.settlement.exception.SettlementStateConflictException;
import com.app.maria.domain.settlement.mapper.SettlementBatchMapper;
import com.app.maria.domain.settlement.mapper.SettlementItemMapper;
import com.app.maria.domain.settlement.mapper.SettlementJoinMapper;
import com.app.maria.domain.settlement.provider.ExchangeRateProvider;
import com.app.maria.domain.settlement.type.BatchStatus;
import com.app.maria.global.exception.ExchangeRateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementBatchTaskletTest {

  @Mock private SettlementBatchMapper settlementBatchMapper;
  @Mock private SettlementItemMapper settlementItemMapper;
  @Mock private SettlementJoinMapper settlementJoinMapper;
  @Mock private ExchangeRateProvider exchangeRateProvider;
  @Mock private SettlementTransactionExecutor settlementTransactionExecutor;
  @Mock private SettlementFailureRecorder settlementFailureRecorder;
  @Mock private SettlementBatchStatusUpdater settlementBatchStatusUpdater;
  @Mock private StepContribution contribution;

  private StepExecution stepExecution;
  private SettlementBatchTasklet tasklet;

  @BeforeEach
  void setUp() {
    tasklet = new SettlementBatchTasklet(
        settlementBatchMapper,
        settlementItemMapper,
        settlementJoinMapper,
        exchangeRateProvider,
        settlementTransactionExecutor,
        settlementFailureRecorder,
        settlementBatchStatusUpdater
    );
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("batchId", 1L)
        .addString("runId", "run-1")
        .toJobParameters();
    JobExecution jobExecution = new JobExecution(1L, jobParameters);
    stepExecution = new StepExecution("settlementStep", jobExecution);
  }

  @Test
  void processesPageAndStoresLastItemId() {
    SettlementBatchDTO batch = batch();
    SettlementItemDTO item = SettlementItemDTO.builder().itemId(10L).batchId(1L).build();
    SettlementJoinDTO target = SettlementJoinDTO.builder()
        .itemId(10L).batchId(1L).purchaseCurrency("USD").build();
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of(item));
    when(settlementJoinMapper.selectTargetByItemId(any())).thenReturn(Optional.of(target));
    when(exchangeRateProvider.getFinalRate("USD", LocalDateTime.of(2026, 8, 4, 9, 0).toLocalDate()))
        .thenReturn(new BigDecimal("1400"));

    RepeatStatus status = tasklet.execute(contribution,
        new ChunkContext(new StepContext(stepExecution)));

    assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);
    assertThat(stepExecution.getExecutionContext().getLong("settlement.lastItemId"))
        .isEqualTo(10L);
    verify(settlementTransactionExecutor).execute(target, new BigDecimal("1400"));
    verify(settlementFailureRecorder, never()).markFailed(any(), any());
  }

  @Test
  void completesBatchWhenNoPendingItemsRemain() {
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch()));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of());
    when(settlementItemMapper.countPendingItems(1L)).thenReturn(0);

    RepeatStatus status = tasklet.execute(contribution,
        new ChunkContext(new StepContext(stepExecution)));

    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    verify(settlementBatchStatusUpdater).completeFromLatestItems(1L);
  }

  @Test
  void cachesRateForSameCurrencyAndDateWithinPage() {
    SettlementBatchDTO batch = batch();
    SettlementItemDTO first = SettlementItemDTO.builder().itemId(10L).batchId(1L).build();
    SettlementItemDTO second = SettlementItemDTO.builder().itemId(11L).batchId(1L).build();
    SettlementJoinDTO firstTarget = target(10L);
    SettlementJoinDTO secondTarget = target(11L);
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of(first, second));
    when(settlementJoinMapper.selectTargetByItemId(any()))
        .thenReturn(Optional.of(firstTarget), Optional.of(secondTarget));
    when(exchangeRateProvider.getFinalRate("USD", batch.getExecutedAt().toLocalDate()))
        .thenReturn(new BigDecimal("1400"));

    tasklet.execute(contribution, new ChunkContext(new StepContext(stepExecution)));

    verify(exchangeRateProvider, times(1))
        .getFinalRate("USD", batch.getExecutedAt().toLocalDate());
    verify(settlementTransactionExecutor).execute(firstTarget, new BigDecimal("1400"));
    verify(settlementTransactionExecutor).execute(secondTarget, new BigDecimal("1400"));
  }

  @Test
  void recordsFailureAndContinuesWhenOneItemFails() {
    SettlementBatchDTO batch = batch();
    SettlementItemDTO item = SettlementItemDTO.builder().itemId(10L).batchId(1L).build();
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of(item));
    when(settlementJoinMapper.selectTargetByItemId(any()))
        .thenReturn(Optional.of(target(10L)));
    when(exchangeRateProvider.getFinalRate(eq("USD"), any()))
        .thenThrow(new ExchangeRateNotFoundException("환율 없음"));

    RepeatStatus status = tasklet.execute(contribution,
        new ChunkContext(new StepContext(stepExecution)));

    assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);
    verify(settlementFailureRecorder).markFailed(eq(10L), any(Exception.class));
    verify(settlementTransactionExecutor, never()).execute(any(), any());
  }

  @Test
  void marksBatchFailedWhenCompletedPageContainsFailedItems() {
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch()));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of());
    when(settlementItemMapper.countPendingItems(1L)).thenReturn(0);

    tasklet.execute(contribution, new ChunkContext(new StepContext(stepExecution)));

    verify(settlementBatchStatusUpdater).completeFromLatestItems(1L);
  }

  @Test
  void doesNotCompleteBatchWhenPendingItemsRemain() {
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch()));
    when(settlementItemMapper.selectPendingItems(any())).thenReturn(List.of());
    when(settlementItemMapper.countPendingItems(1L)).thenReturn(1);

    assertThatThrownBy(() ->
        tasklet.execute(contribution, new ChunkContext(new StepContext(stepExecution))))
        .isInstanceOf(SettlementStateConflictException.class);

    verify(settlementBatchStatusUpdater, never()).completeFromLatestItems(any());
  }

  @Test
  void rejectsJobWhenRunIdDoesNotMatchCurrentBatchRunId() {
    SettlementBatchDTO batch = batch();
    batch.setRunId("new-run-id");
    when(settlementBatchMapper.selectBatchById(1L)).thenReturn(Optional.of(batch));

    assertThatThrownBy(() ->
        tasklet.execute(contribution, new ChunkContext(new StepContext(stepExecution))))
        .isInstanceOf(SettlementStateConflictException.class);
  }

  private SettlementBatchDTO batch() {
    return SettlementBatchDTO.builder()
        .batchId(1L)
        .runId("run-1")
        .status(BatchStatus.RUNNING)
        .executedAt(LocalDateTime.of(2026, 8, 4, 9, 0))
        .build();
  }

  private SettlementJoinDTO target(Long itemId) {
    return SettlementJoinDTO.builder()
        .itemId(itemId)
        .batchId(1L)
        .purchaseCurrency("USD")
        .build();
  }
}
