package com.app.maria.domain.settlement.mapper;

import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Mapper
public interface SettlementBatchMapper {

  List<SettlementBatchDTO> selectBatches();

  Optional<SettlementBatchDTO> selectBatchById(Long batchId);

  Optional<SettlementBatchDTO> selectBatchByIdForUpdate(Long batchId);

  int insertBatch(SettlementBatchDTO batch);

  Optional<SettlementBatchDTO> selectBatchByRunId(String runId);

  Optional<SettlementBatchDTO> selectRunningBatchByBusinessDate(LocalDate businessDate);

  Optional<SettlementBatchDTO> selectBatchByBusinessDate(LocalDate businessDate);

  int countRunningBatch(SettlementBatchDTO batch);

  int updateBatchStatus(SettlementBatchDTO batch);

  int refreshBatchStatusAfterRetry(Long batchId);

  int markBatchRetryRunning(SettlementBatchDTO batch);
}
