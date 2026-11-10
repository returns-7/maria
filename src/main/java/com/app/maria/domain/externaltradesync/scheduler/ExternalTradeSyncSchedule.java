package com.app.maria.domain.externaltradesync.scheduler;

import com.app.maria.domain.externaltradesync.launcher.ExternalTradeSyncLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalTradeSyncSchedule {

    private final ExternalTradeSyncLauncher externalTradeSyncLauncher;

    @Scheduled(cron = "0 0 1 * * *")
    public void run() {
        externalTradeSyncLauncher.launch();
    }
}
