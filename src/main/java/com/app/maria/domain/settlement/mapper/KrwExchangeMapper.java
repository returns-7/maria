package com.app.maria.domain.settlement.mapper;

import com.app.maria.domain.settlement.dto.KrwExchangeDTO;
import com.app.maria.domain.settlement.type.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KrwExchangeMapper {

    Optional<KrwExchangeDTO> selectExchangeById(Long exchangeId);

    Optional<KrwExchangeDTO> selectExchangeByIdForUpdate(Long exchangeId);

    Optional<BigDecimal> selectAccountAmountForUpdate(Long accountId);

    int finalizeExchange(KrwExchangeDTO krwExchangeDTO);

    int replaceAccountAmount(KrwExchangeDTO krwExchangeDTO);

    int insertLeftAmount(KrwExchangeDTO krwExchangeDTO);

    int insertProvisional(KrwExchangeDTO krwExchangeDTO);

    int countByStatus(SettlementStatus settlementStatus);

    BigDecimal sumProvisionalAmountByStatus(SettlementStatus settlementStatus);

    BigDecimal sumFinalizedAmountBetween(LocalDateTime start, LocalDateTime end);
}
