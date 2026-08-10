package com.app.maria.domain.settlement.scheduler;

import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementScheduleTest {

  @Mock private SettlementService settlementService;
  @InjectMocks private SettlementSchedule settlementSchedule;

  @Test
  void executesSettlementBatchAtScheduledTime() {
    when(settlementService.executeSettlementBatch()).thenReturn(SettlementBatchDTO.builder()
        .batchId(1L)
        .runId("run-id")
        .build());

    settlementSchedule.executeDailySettlement();

    verify(settlementService).executeSettlementBatch();
  }
}
