package com.app.maria.domain.settlement.scheduler;

import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementSchedule {

  private final SettlementService
      settlementService;

  @Scheduled(
      cron = "${custom.settlement.cron:0 0 0 * * *}",
      zone = "${custom.settlement.zone:Asia/Seoul}"
  )
  public void executeDailySettlement() {
    try {
      SettlementBatchDTO batch = settlementService.executeSettlementBatch();
      log.info("일일 확정산 Batch 실행 요청 완료. batchId={}, runId={}", batch.getBatchId(), batch.getRunId());
    } catch (Exception e) {
      log.error("일일 확정산 Batch 실행 요청실패", e);
    }
  }
}
