package com.app.maria.domain.externaltradesync.launcher;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.app.maria.domain.externaltradesync.service.ExternalTradeSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalTradeSyncLauncherTest {

    @Mock private ExternalTradeSyncService externalTradeSyncService;

    @InjectMocks private ExternalTradeSyncLauncher externalTradeSyncLauncher;

    @Test
    @DisplayName("syncAll()을 호출한다")
    void launchCallsSyncAll() {
        externalTradeSyncLauncher.launch();

        verify(externalTradeSyncService).syncAll();
    }

    @Test
    @DisplayName("syncAll()이 예외를 던져도 launch()는 예외를 밖으로 전파하지 않는다")
    void launchDoesNotPropagateExceptionFromSyncAll() {
        doThrow(new RuntimeException("mydata 서버 연결 실패")).when(externalTradeSyncService).syncAll();

        assertThatCode(() -> externalTradeSyncLauncher.launch()).doesNotThrowAnyException();

        verify(externalTradeSyncService).syncAll();
    }
}
