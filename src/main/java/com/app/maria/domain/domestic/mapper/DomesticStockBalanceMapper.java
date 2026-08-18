package com.app.maria.domain.domestic.mapper;

import com.app.maria.domain.domestic.dto.DomesticHoldingDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentListDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentSearchDTO;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DomesticStockBalanceMapper {
    List<DomesticInvestmentListDTO> selectAccountSummaries(DomesticInvestmentSearchDTO condition);

    int countAccountSummaries(DomesticInvestmentSearchDTO condition);

    Optional<DomesticInvestmentListDTO> selectAccountSummaryById(
            @Param("accountId") Long accountId);

    List<DomesticHoldingDTO> selectHoldingsByAccountId(@Param("accountId") Long accountId);
}
