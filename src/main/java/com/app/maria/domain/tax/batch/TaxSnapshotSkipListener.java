package com.app.maria.domain.tax.batch;

import com.app.maria.domain.tax.dto.TaxSnapshotDTO;
import com.app.maria.domain.tax.dto.TaxSnapshotTargetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaxSnapshotSkipListener implements SkipListener<TaxSnapshotTargetDTO, TaxSnapshotDTO> {
    @Override
    public void onSkipInProcess(TaxSnapshotTargetDTO target, Throwable t) {
        log.error("세액 스냅샷 계산 실패. accountId={}", target.getAccount().getAccountId(), t);
    }

    @Override
    public void onSkipInWrite(TaxSnapshotDTO snapshot, Throwable t) {
        log.error("세액 스냅샷 저장 실패. accountId={}", snapshot.getAccountId(), t);
    }
}
