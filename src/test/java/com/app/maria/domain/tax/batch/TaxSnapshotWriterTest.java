package com.app.maria.domain.tax.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.app.maria.domain.account.service.AccountTransactionalService;
import com.app.maria.domain.account.type.BenefitType;
import com.app.maria.domain.tax.dto.TaxSnapshotDTO;
import com.app.maria.domain.tax.mapper.TaxSnapshotMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class TaxSnapshotWriterTest {

    private static final LocalDateTime CALCULATED_AT = LocalDateTime.of(2026, 8, 15, 2, 0);

    @Mock private TaxSnapshotMapper taxSnapshotMapper;

    @Mock private AccountTransactionalService accountTransactionalService;

    private TaxSnapshotWriter writer;

    @BeforeEach
    void setUp() {
        writer = new TaxSnapshotWriter(taxSnapshotMapper, accountTransactionalService);
    }

    private TaxSnapshotDTO snapshot(long accountId, String weightedExternalAmount) {
        return TaxSnapshotDTO.builder()
                .accountId(accountId)
                .calculatedAt(CALCULATED_AT)
                .weightedExternalAmount(new BigDecimal(weightedExternalAmount))
                .build();
    }

    @Test
    @DisplayName("chunk에 담긴 스냅샷을 한 번에 upsert한다")
    void write_chunk을_그대로_upsert에_넘긴다() {
        TaxSnapshotDTO a = snapshot(1L, "0");
        TaxSnapshotDTO b = snapshot(2L, "0");

        writer.write(new Chunk<>(List.of(a, b)));

        ArgumentCaptor<List<TaxSnapshotDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(taxSnapshotMapper).upsertSnapshots(captor.capture());
        assertThat(captor.getValue()).containsExactly(a, b);
    }

    @Test
    @DisplayName("빈 chunk는 upsert도, benefit 변경도 호출하지 않는다")
    void write_빈chunk은_호출하지_않는다() {
        writer.write(new Chunk<>());

        verify(taxSnapshotMapper, never()).upsertSnapshots(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(accountTransactionalService);
    }

    @Test
    @DisplayName("외부순매수가 0보다 크면 REDUCED로 전환한다")
    void write_외부순매수있으면_REDUCED로_전환() {
        TaxSnapshotDTO snapshot = snapshot(1L, "4000000");

        writer.write(new Chunk<>(List.of(snapshot)));

        verify(accountTransactionalService)
                .changeBenefit(
                        eq(1L),
                        eq(BenefitType.REDUCED),
                        org.mockito.ArgumentMatchers.any(),
                        eq(CALCULATED_AT));
    }

    @Test
    @DisplayName("외부순매수가 0이면 POSSIBLE로 전환한다")
    void write_외부순매수없으면_POSSIBLE로_전환() {
        TaxSnapshotDTO snapshot = snapshot(1L, "0");

        writer.write(new Chunk<>(List.of(snapshot)));

        verify(accountTransactionalService)
                .changeBenefit(
                        eq(1L),
                        eq(BenefitType.POSSIBLE),
                        org.mockito.ArgumentMatchers.any(),
                        eq(CALCULATED_AT));
    }
}
