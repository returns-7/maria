package com.app.maria.domain.account.service;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.infra.RedisMydataSyncTaskRepository;
import com.app.maria.domain.account.infra.RedisMydataSyncTaskRepository.ClaimedTask;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.account.provider.MydataProvider;
import com.app.maria.domain.account.type.Status;
import com.app.maria.global.exception.MydataApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountMydataSyncServiceTest {
  private static final Long CUSTOMER_ID = 1L;
  private static final String CI_HASH = "ci-hash";

  @Mock private AccountMapper accountMapper;
  @Mock private MydataProvider mydataProvider;
  @Mock private RedisMydataSyncTaskRepository taskRepository;
  @InjectMocks private AccountMydataSyncService service;

  @Test
  void createEnqueuesInitialSynchronizationWithoutCallingMydata() {
    AccountDTO account = openedAccount();

    service.create(account);

    verify(taskRepository).enqueue(10L, "CREATE");
    verify(mydataProvider, never()).createRiaAccount(CI_HASH, account);
  }

  @Test
  void updateLimitEnqueuesSynchronizationWithoutCallingMydata() {
    AccountDTO account = openedAccount();

    service.updateLimit(account);

    verify(taskRepository).enqueue(10L, "UPDATE_LIMIT");
    verify(mydataProvider, never()).updateRiaLimit(CI_HASH, account);
  }

  @Test
  void retryUsesStoredOperationWithoutMydataLookup() {
    AccountDTO missing = openedAccount();
    missing.setAccountId(10L);
    AccountDTO existing = openedAccount();
    existing.setAccountId(11L);
    ClaimedTask createTask = claimed(10L, "CREATE:0:token-10");
    ClaimedTask updateTask = claimed(11L, "UPDATE_LIMIT:0:token-11");
    when(taskRepository.claimDueTasks(100)).thenReturn(List.of(createTask, updateTask));
    when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(missing));
    when(accountMapper.selectByAccountId(11L)).thenReturn(Optional.of(existing));
    when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(CI_HASH));
    when(mydataProvider.createRiaAccount(CI_HASH, missing)).thenReturn(HttpStatus.OK);
    when(mydataProvider.updateRiaLimit(CI_HASH, existing)).thenReturn(HttpStatus.OK);

    service.retryOpenedAccounts();

    verify(mydataProvider).createRiaAccount(CI_HASH, missing);
    verify(mydataProvider).updateRiaLimit(CI_HASH, existing);
    verify(taskRepository).complete(createTask);
    verify(taskRepository).complete(updateTask);
    verify(taskRepository).release(createTask);
    verify(taskRepository).release(updateTask);
  }

  @Test
  void retryReschedulesUpdateWhenMydataUpdateFails() {
    AccountDTO account = openedAccount();
    account.setAccountId(10L);
    ClaimedTask task = claimed(10L, "UPDATE_LIMIT:0:token-10");
    when(taskRepository.claimDueTasks(100)).thenReturn(List.of(task));
    when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(account));
    when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(CI_HASH));
    when(mydataProvider.updateRiaLimit(CI_HASH, account))
        .thenThrow(new MydataApiException("등록되지 않은 RIA 계좌", null));

    service.retryOpenedAccounts();

    verify(mydataProvider, never()).createRiaAccount(CI_HASH, account);
    verify(mydataProvider).updateRiaLimit(CI_HASH, account);
    verify(taskRepository).reschedule(task, "UPDATE_LIMIT");
    verify(taskRepository).release(task);
  }

  @Test
  void retryPreservesCreateOperationWhenLookupFails() {
    AccountDTO account = openedAccount();
    ClaimedTask task = claimed(10L, "CREATE:0:token-10");
    when(taskRepository.claimDueTasks(100)).thenReturn(List.of(task));
    when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(account));
    when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.empty());

    service.retryOpenedAccounts();

    verify(mydataProvider, never()).createRiaAccount(CI_HASH, account);
    verify(mydataProvider, never()).updateRiaLimit(CI_HASH, account);
    verify(taskRepository).reschedule(task, "CREATE");
    verify(taskRepository).release(task);
  }

  private ClaimedTask claimed(Long accountId, String value) {
    return new ClaimedTask(accountId, value, "lock:" + accountId, "token:" + accountId);
  }

  private AccountDTO openedAccount() {
    return AccountDTO.builder()
        .accountId(10L)
        .customerId(CUSTOMER_ID)
        .status(Status.OPENED)
        .limitAmount(BigDecimal.valueOf(30_000_000L))
        .build();
  }
}
