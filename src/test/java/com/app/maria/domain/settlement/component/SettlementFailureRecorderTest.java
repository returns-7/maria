package com.app.maria.domain.settlement.component;

import com.app.maria.domain.settlement.dto.SettlementItemDTO;
import com.app.maria.domain.settlement.exception.SettlementStateConflictException;
import com.app.maria.domain.settlement.exception.SettlementCalculationException;
import com.app.maria.domain.settlement.mapper.SettlementItemMapper;
import com.app.maria.domain.settlement.type.SettlementFailureCode;
import com.app.maria.domain.settlement.type.SettlementItemResult;
import com.app.maria.global.clock.service.BusinessClockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementFailureRecorderTest {

  @Mock
  private SettlementItemMapper settlementItemMapper;
  @Mock
  private BusinessClockService clockService;

  @Test
  void recordsFailedItemInIndependentComponent() {
    LocalDateTime now = LocalDateTime.of(2026, 8, 7, 16, 0);
    when(clockService.now()).thenReturn(now);
    when(settlementItemMapper.updateItemResult(any(SettlementItemDTO.class))).thenReturn(1);
    SettlementFailureRecorder recorder = new SettlementFailureRecorder(settlementItemMapper, clockService);

    recorder.markFailed(10L, new IllegalStateException("실패"));

    ArgumentCaptor<SettlementItemDTO> captor =
        ArgumentCaptor.forClass(SettlementItemDTO.class);
    verify(settlementItemMapper).updateItemResult(captor.capture());
    assertThat(captor.getValue().getItemId()).isEqualTo(10L);
    assertThat(captor.getValue().getResult()).isEqualTo(SettlementItemResult.FAILED);
    assertThat(captor.getValue().getProcessedAt()).isEqualTo(now);
    assertThat(captor.getValue().getFailureMessage()).isEqualTo("실패");
  }

  @Test
  void rejectsWhenFailureUpdateDoesNotAffectOneRow() {
    when(settlementItemMapper.updateItemResult(any(SettlementItemDTO.class))).thenReturn(0);
    SettlementFailureRecorder recorder = new SettlementFailureRecorder(settlementItemMapper, clockService);

    assertThatThrownBy(() -> recorder.markFailed(10L, new IllegalStateException("실패")))
        .isInstanceOf(SettlementStateConflictException.class);
  }

  @Test
  void classifiesCalculationFailureSeparately() {
    when(settlementItemMapper.updateItemResult(any(SettlementItemDTO.class))).thenReturn(1);
    SettlementFailureRecorder recorder = new SettlementFailureRecorder(settlementItemMapper, clockService);

    recorder.markFailed(10L, new SettlementCalculationException("계산 실패"));

    ArgumentCaptor<SettlementItemDTO> captor = ArgumentCaptor.forClass(SettlementItemDTO.class);
    verify(settlementItemMapper).updateItemResult(captor.capture());
    assertThat(captor.getValue().getFailureCode())
        .isEqualTo(SettlementFailureCode.CALCULATION_ERROR);
  }

  @Test
  void rejectsNullItemIdWithoutMapperCall() {
    SettlementFailureRecorder recorder = new SettlementFailureRecorder(settlementItemMapper, clockService);

    assertThatThrownBy(() -> recorder.markFailed(null, new IllegalStateException("실패")))
        .isInstanceOf(SettlementStateConflictException.class);

    verify(settlementItemMapper, never()).updateItemResult(any());
  }
}
