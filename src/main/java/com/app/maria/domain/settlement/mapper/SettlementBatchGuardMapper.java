package com.app.maria.domain.settlement.mapper;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettlementBatchGuardMapper {

    int ensureGuard(LocalDate businessDate);

    Optional<LocalDate> selectGuardForUpdate(LocalDate businessDate);
}
