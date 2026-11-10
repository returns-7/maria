package com.app.maria.domain.tax.batch;

import com.app.maria.domain.account.service.AccountTransactionalService;
import com.app.maria.domain.account.type.BenefitType;
import com.app.maria.domain.tax.dto.TaxSnapshotDTO;
import com.app.maria.domain.tax.mapper.TaxSnapshotMapper;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaxSnapshotWriter implements ItemWriter<TaxSnapshotDTO> {
    private static final String BENEFIT_CHANGE_REASON = "일별 세액 스냅샷 배치 판정";

    private final TaxSnapshotMapper taxSnapshotMapper;
    private final AccountTransactionalService accountTransactionalService;

    @Override
    public void write(Chunk<? extends TaxSnapshotDTO> chunk) {
        if (chunk.isEmpty()) {
            return;
        }
        taxSnapshotMapper.upsertSnapshots(new ArrayList<>(chunk.getItems()));

        for (TaxSnapshotDTO snapshot : chunk) {
            BenefitType newBenefit =
                    snapshot.getWeightedExternalAmount().signum() > 0
                            ? BenefitType.REDUCED
                            : BenefitType.POSSIBLE;
            accountTransactionalService.changeBenefit(
                    snapshot.getAccountId(),
                    newBenefit,
                    BENEFIT_CHANGE_REASON,
                    snapshot.getCalculatedAt());
        }
    }
}
