package com.app.maria.domain.settlement.service;

import com.app.maria.domain.settlement.dto.KrwExchangeDTO;
import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.dto.SettlementItemDTO;
import com.app.maria.domain.settlement.dto.SettlementJoinDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SettlementService {

    SettlementBatchDTO executeSettlementBatch();

    SettlementBatchDTO executeSettlementBatchByAdmin();

    List<SettlementBatchDTO> getSettlementBatches();

    SettlementBatchDTO getSettlementBatch(Long batchId);

    List<SettlementJoinDTO> getSettlementBatchDetail(Long batchId);

    List<SettlementJoinDTO> getSettlementBatchFailDetail(Long batchId);

    SettlementBatchDTO getSettlementBatchByRunId(String runId);

    List<SettlementItemDTO> getPendingSettlementItems(Long batchId, Long lastItemId);

    SettlementJoinDTO getSettlementItem(Long batchId, Long itemId);

    SettlementItemDTO retryFailedSettlementItem(Long batchId, Long itemId);

    SettlementBatchDTO retryFailedSettlementBatch(Long batchId);

    KrwExchangeDTO getKrwExchange(Long exchangeId);

    int getProvisionalExchangeCount();

    BigDecimal getPendingProvisionalAmount();

    BigDecimal getFinalizedAmountBetween(LocalDateTime start, LocalDateTime end);
}
