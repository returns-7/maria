package com.app.maria.domain.tax.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class TaxSnapshotJobLauncherTest {

    private static final LocalDateTime CALCULATED_AT = LocalDateTime.of(2026, 8, 15, 2, 0);

    @Mock private JobLauncher jobLauncher;
    @Mock private Job taxSnapshotJob;

    private TaxSnapshotJobLauncher launcher;

    private JobExecution execution(long id) {
        JobExecution execution = new JobExecution(id);
        execution.setStatus(BatchStatus.COMPLETED);
        return execution;
    }

    @Test
    @DisplayName("calculatedAt을 JobParameters에 담아 taxSnapshotJob을 실행한다")
    void launch_calculatedAt을_파라미터로_넘긴다() throws Exception {
        launcher = new TaxSnapshotJobLauncher(jobLauncher, taxSnapshotJob);
        when(jobLauncher.run(eq(taxSnapshotJob), any())).thenReturn(execution(1L));

        JobExecution result = launcher.launch(CALCULATED_AT);

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        org.mockito.Mockito.verify(jobLauncher).run(eq(taxSnapshotJob), captor.capture());
        assertThat(captor.getValue().getLocalDateTime("calculatedAt")).isEqualTo(CALCULATED_AT);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("호출마다 다른 runId를 발급해 같은 날 여러 번 실행해도 JobInstance가 겹치지 않는다")
    void launch_매번_다른_runId를_발급한다() throws Exception {
        launcher = new TaxSnapshotJobLauncher(jobLauncher, taxSnapshotJob);
        when(jobLauncher.run(eq(taxSnapshotJob), any()))
                .thenReturn(execution(1L))
                .thenReturn(execution(2L));

        launcher.launch(CALCULATED_AT);
        launcher.launch(CALCULATED_AT);

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        org.mockito.Mockito.verify(jobLauncher, org.mockito.Mockito.times(2))
                .run(eq(taxSnapshotJob), captor.capture());
        var runIds = captor.getAllValues().stream().map(p -> p.getString("runId")).toList();
        assertThat(runIds.get(0)).isNotEqualTo(runIds.get(1));
    }
}
