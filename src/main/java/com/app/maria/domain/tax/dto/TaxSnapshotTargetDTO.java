package com.app.maria.domain.tax.dto;

import com.app.maria.domain.account.dto.AccountDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxSnapshotTargetDTO {
    private AccountDTO account;
    private List<SellLotDTO> sellLots;
    private List<ExternalBuyDTO> externalTrades;

    public static TaxSnapshotTargetDTO of(
            AccountDTO account, List<SellLotDTO> sellLots, List<ExternalBuyDTO> externalTrades) {
        return TaxSnapshotTargetDTO.builder()
                .account(account)
                .sellLots(sellLots)
                .externalTrades(externalTrades)
                .build();
    }
}
