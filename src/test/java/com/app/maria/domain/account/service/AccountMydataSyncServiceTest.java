package com.app.maria.domain.account.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.infra.RedisMydataSyncTaskRepository;
import com.app.maria.domain.account.infra.RedisMydataSyncTaskRepository.ClaimedTask;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.account.provider.MydataProvider;
import com.app.maria.domain.account.type.Status;
import com.app.maria.global.exception.MydataApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AccountMydataSyncServiceTest {
    private static final Long CUSTOMER_ID = 1L;
    private static final String CI_HASH = "ci-hash";

    @Mock private AccountMapper accountMapper;
    @Mock private MydataProvider mydataProvider;
    @Mock private RedisMydataSyncTaskRepository taskRepository;
    @InjectMocks private AccountMydataSyncService service;

    @Test
    void createEnqueuesSyncWithoutCallingMydata() {
        AccountDTO account = openedAccount(10L, "30000000");

        service.create(account);

        verify(taskRepository).enqueue(10L, "SYNC");
        verify(mydataProvider, never()).syncRiaAccount(CI_HASH, account);
    }

    @Test
    void updateLimitEnqueuesSyncWithoutCallingMydata() {
        AccountDTO account = openedAccount(10L, "40000000");

        service.updateLimit(account);

        verify(taskRepository).enqueue(10L, "SYNC");
        verify(mydataProvider, never()).syncRiaAccount(CI_HASH, account);
    }

    @Test
    void retrySyncsLatestAccountStateWithMydataSave() {
        AccountDTO account = openedAccount(10L, "40000000");
        ClaimedTask task = claimed(10L, "SYNC:0:token-10");
        when(taskRepository.claimDueTasks(100)).thenReturn(List.of(task));
        when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(account));
        when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(CI_HASH));
        when(mydataProvider.syncRiaAccount(CI_HASH, account)).thenReturn(HttpStatus.OK);

        service.retryOpenedAccounts();

        verify(mydataProvider).syncRiaAccount(CI_HASH, account);
        verify(taskRepository).complete(task);
        verify(taskRepository).release(task);
    }

    @Test
    void retryTreatsLegacyCreateAndUpdateTasksAsSync() {
        AccountDTO createAccount = openedAccount(10L, "30000000");
        AccountDTO updateAccount = openedAccount(11L, "40000000");
        ClaimedTask createTask = claimed(10L, "CREATE:0:token-10");
        ClaimedTask updateTask = claimed(11L, "UPDATE_LIMIT:0:token-11");
        when(taskRepository.claimDueTasks(100)).thenReturn(List.of(createTask, updateTask));
        when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(createAccount));
        when(accountMapper.selectByAccountId(11L)).thenReturn(Optional.of(updateAccount));
        when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(CI_HASH));
        when(mydataProvider.syncRiaAccount(CI_HASH, createAccount)).thenReturn(HttpStatus.OK);
        when(mydataProvider.syncRiaAccount(CI_HASH, updateAccount)).thenReturn(HttpStatus.OK);

        service.retryOpenedAccounts();

        verify(mydataProvider).syncRiaAccount(CI_HASH, createAccount);
        verify(mydataProvider).syncRiaAccount(CI_HASH, updateAccount);
        verify(taskRepository).complete(createTask);
        verify(taskRepository).complete(updateTask);
    }

    @Test
    void retryReschedulesAsSyncWhenMydataSaveFails() {
        AccountDTO account = openedAccount(10L, "40000000");
        ClaimedTask task = claimed(10L, "SYNC:0:token-10");
        when(taskRepository.claimDueTasks(100)).thenReturn(List.of(task));
        when(accountMapper.selectByAccountId(10L)).thenReturn(Optional.of(account));
        when(accountMapper.selectCiHashByCustomerId(CUSTOMER_ID)).thenReturn(Optional.of(CI_HASH));
        when(mydataProvider.syncRiaAccount(CI_HASH, account))
                .thenThrow(new MydataApiException("동기화 실패", null));

        service.retryOpenedAccounts();

        verify(taskRepository).reschedule(task, "SYNC");
        verify(taskRepository).release(task);
    }

    private ClaimedTask claimed(Long accountId, String value) {
        return new ClaimedTask(accountId, value, "lock:" + accountId, "token:" + accountId);
    }

    private AccountDTO openedAccount(Long accountId, String limitAmount) {
        return AccountDTO.builder()
                .accountId(accountId)
                .customerId(CUSTOMER_ID)
                .status(Status.OPENED)
                .limitAmount(new BigDecimal(limitAmount))
                .build();
    }
}
