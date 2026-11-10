package com.app.maria.domain.externaltradesync.launcher;

import com.app.maria.domain.externaltradesync.dto.response.ExternalTradeSyncResultDTO;
import com.app.maria.domain.externaltradesync.service.ExternalTradeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalTradeSyncLauncher {

    private final ExternalTradeSyncService externalTradeSyncService;

    @Async("externalTradeSyncTaskExecutor")
    public void launch() {
        try {
            ExternalTradeSyncResultDTO result = externalTradeSyncService.syncAll();
            log.info(
                    "외부 순매수 동기화 완료 - 대상 고객 {}명, 신규 판정 {}건, 스킵 {}건, 실패 고객 {}명",
                    result.getCustomerCount(),
                    result.getNewJudgementCount(),
                    result.getSkippedJudgementCount(),
                    result.getFailedCustomerCount());
        } catch (Exception e) {
            log.error("외부 순매수 동기화 실행 중 예외 발생", e);
        }
    }
}
