package com.app.maria.domain.settlement.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.maria.domain.settlement.component.SettlementBatchStatusUpdater;
import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.type.BatchStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class SettlementBatchLauncherTest {

    @Mock private JobLauncher jobLauncher;

    @Mock private Job settlementJob;

    @Mock private SettlementBatchStatusUpdater statusUpdater;

    @Test
    void launchesJobWithBatchParameters() throws Exception {
        SettlementBatchLauncher launcher =
                new SettlementBatchLauncher(jobLauncher, settlementJob, statusUpdater);
        SettlementBatchDTO batch =
                SettlementBatchDTO.builder()
                        .batchId(1L)
                        .runId("run-1")
                        .status(BatchStatus.RUNNING)
                        .build();

        launcher.launch(batch);

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(any(Job.class), captor.capture());
        assertThat(captor.getValue().getLong("batchId")).isEqualTo(1L);
        assertThat(captor.getValue().getString("runId")).isEqualTo("run-1");
    }

    @Test
    void marksBatchFailedWhenJobLaunchFails() throws Exception {
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenThrow(new IllegalStateException("launch failed"));
        SettlementBatchLauncher launcher =
                new SettlementBatchLauncher(jobLauncher, settlementJob, statusUpdater);
        SettlementBatchDTO batch =
                SettlementBatchDTO.builder()
                        .batchId(1L)
                        .runId("run-1")
                        .status(BatchStatus.RUNNING)
                        .build();

        launcher.launch(batch);

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.RUNNING);
        verify(statusUpdater).markFailedIfRunning(1L, "launch failed");
    }
}
