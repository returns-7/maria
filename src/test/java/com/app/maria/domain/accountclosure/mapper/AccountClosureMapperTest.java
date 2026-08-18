package com.app.maria.domain.accountclosure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.accountclosure.dto.AccountClosureDTO;
import com.app.maria.domain.accountclosure.type.AccountClosureStatus;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountClosureMapperTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 8, 12, 9, 0);
    private static final LocalDateTime PROCESSED_AT = LocalDateTime.of(2026, 8, 12, 10, 0);

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private AccountClosureMapper accountClosureMapper;
    private AccountMapper accountMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader =
                Resources.getResourceAsReader("mybatis-accountclosure-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource =
                (PooledDataSource)
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
    }

    @BeforeEach
    void setUp() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        accountClosureMapper = sqlSession.getMapper(AccountClosureMapper.class);
        accountMapper = sqlSession.getMapper(AccountMapper.class);
    }

    @AfterEach
    void closeSession() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) {
            dataSource.forceCloseAll();
        }
    }

    @Test
    @DisplayName("해지 신청을 저장하고 생성된 ID와 신청 정보를 다시 조회한다")
    void insertAndSelectClosureRequest() {
        AccountClosureDTO request = closureRequest(true);

        assertThat(accountClosureMapper.insertClosureRequest(request)).isOne();
        assertThat(request.getClosureRequestId()).isNotNull();

        AccountClosureDTO saved =
                accountClosureMapper
                        .selectByIdForUpdate(request.getClosureRequestId())
                        .orElseThrow();
        assertThat(saved.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getDestinationGeneralAccountId()).isEqualTo(20L);
        assertThat(saved.isEarlyWithdrawalAgreed()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(AccountClosureStatus.REQUESTED);
        assertThat(saved.getRequestedAt()).isEqualTo(REQUESTED_AT);
        assertThat(saved.getProcessedAt()).isNull();
        assertThat(saved.getProcessedBy()).isNull();
        assertThat(saved.getWithdrawalId()).isNull();
    }

    @Test
    @DisplayName("REQUESTED 해지 신청은 한 번만 완료 처리된다")
    void completeOnlyRequestedClosureOnce() {
        AccountClosureDTO request = insertClosureRequest();
        request.setProcessedAt(PROCESSED_AT);
        request.setProcessedBy(7L);
        request.setWithdrawalId(30L);

        assertThat(accountClosureMapper.completeClosureRequest(request)).isOne();
        assertThat(accountClosureMapper.completeClosureRequest(request)).isZero();

        AccountClosureDTO completed =
                accountClosureMapper
                        .selectByIdForUpdate(request.getClosureRequestId())
                        .orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(AccountClosureStatus.COMPLETED);
        assertThat(completed.getProcessedAt()).isEqualTo(PROCESSED_AT);
        assertThat(completed.getProcessedBy()).isEqualTo(7L);
        assertThat(completed.getWithdrawalId()).isEqualTo(30L);
    }

    @Test
    @DisplayName("REQUESTED 해지 신청은 반려 사유와 함께 한 번만 반려된다")
    void rejectOnlyRequestedClosureOnce() {
        AccountClosureDTO request = insertClosureRequest();
        request.setProcessedAt(PROCESSED_AT);
        request.setProcessedBy(8L);
        request.setRejectionReason("해지 목적지 계좌 확인 실패");

        assertThat(accountClosureMapper.rejectClosureRequest(request)).isOne();
        assertThat(accountClosureMapper.rejectClosureRequest(request)).isZero();

        AccountClosureDTO rejected =
                accountClosureMapper
                        .selectByIdForUpdate(request.getClosureRequestId())
                        .orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(AccountClosureStatus.REJECTED);
        assertThat(rejected.getProcessedAt()).isEqualTo(PROCESSED_AT);
        assertThat(rejected.getProcessedBy()).isEqualTo(8L);
        assertThat(rejected.getRejectionReason()).isEqualTo("해지 목적지 계좌 확인 실패");
        assertThat(rejected.getWithdrawalId()).isNull();
    }

    @Test
    @DisplayName("계좌는 OPENED에서 해지 신청 상태로 한 번만 전환된다")
    void requestClosureOnlyFromOpened() throws SQLException {
        assertThat(accountMapper.requestClosure(ACCOUNT_ID)).isOne();
        assertThat(accountMapper.requestClosure(ACCOUNT_ID)).isZero();
        assertThat(accountStatus()).isEqualTo("CLOSURE_REQUESTED");
    }

    @Test
    @DisplayName("해지 반려 시 신청 상태 계좌를 OPENED로 복귀시킨다")
    void reopenAfterClosureRejection() throws SQLException {
        assertThat(accountMapper.requestClosure(ACCOUNT_ID)).isOne();
        assertThat(accountMapper.reopenAfterClosureRejection(ACCOUNT_ID)).isOne();
        assertThat(accountMapper.reopenAfterClosureRejection(ACCOUNT_ID)).isZero();
        assertThat(accountStatus()).isEqualTo("OPENED");
    }

    @Test
    @DisplayName("잔액이 0원인 해지 신청 상태 계좌만 CLOSED로 전환한다")
    void completeClosureOnlyWhenBalanceIsZero() throws SQLException {
        assertThat(accountMapper.requestClosure(ACCOUNT_ID)).isOne();
        assertThat(accountMapper.completeClosure(ACCOUNT_ID)).isZero();
        assertThat(accountStatus()).isEqualTo("CLOSURE_REQUESTED");

        updateAccountAmount(BigDecimal.ZERO);

        assertThat(accountMapper.completeClosure(ACCOUNT_ID)).isOne();
        assertThat(accountMapper.completeClosure(ACCOUNT_ID)).isZero();
        assertThat(accountStatus()).isEqualTo("CLOSED");
    }

    @Test
    void selectByStatusReturnsOnlyMatchingClosuresInOldestFirstOrder() {
        AccountClosureDTO laterRequested = closureRequest(false);
        laterRequested.setRequestedAt(REQUESTED_AT.plusHours(1));
        accountClosureMapper.insertClosureRequest(laterRequested);

        AccountClosureDTO firstAtSameTime = closureRequest(true);
        firstAtSameTime.setRequestedAt(REQUESTED_AT);
        accountClosureMapper.insertClosureRequest(firstAtSameTime);

        AccountClosureDTO secondAtSameTime = closureRequest(false);
        secondAtSameTime.setRequestedAt(REQUESTED_AT);
        accountClosureMapper.insertClosureRequest(secondAtSameTime);

        secondAtSameTime.setProcessedAt(PROCESSED_AT);
        secondAtSameTime.setProcessedBy(7L);
        secondAtSameTime.setRejectionReason("조회 대상 제외");
        assertThat(accountClosureMapper.rejectClosureRequest(secondAtSameTime)).isOne();

        List<AccountClosureDTO> requested =
                accountClosureMapper.selectByStatus(AccountClosureStatus.REQUESTED);

        assertThat(requested)
                .extracting(AccountClosureDTO::getClosureRequestId)
                .containsExactly(
                        firstAtSameTime.getClosureRequestId(),
                        laterRequested.getClosureRequestId());
        assertThat(requested)
                .allMatch(closure -> closure.getStatus() == AccountClosureStatus.REQUESTED);
        assertThat(requested)
                .allSatisfy(
                        closure -> {
                            assertThat(closure.getCustomerName()).isEqualTo("조회 고객");
                            assertThat(closure.getAccountNo()).isEqualTo("1234567890");
                            assertThat(closure.getAccountAmount()).isEqualByComparingTo("1000");
                        });
    }

    @Test
    void selectByIdReturnsClosureWithoutProcessingLockQuery() {
        AccountClosureDTO request = insertClosureRequest();

        AccountClosureDTO found =
                accountClosureMapper.selectById(request.getClosureRequestId()).orElseThrow();

        assertThat(found.getClosureRequestId()).isEqualTo(request.getClosureRequestId());
        assertThat(found.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(found.getCustomerName()).isEqualTo("조회 고객");
        assertThat(found.getAccountNo()).isEqualTo("1234567890");
        assertThat(found.getAccountAmount()).isEqualByComparingTo("1000");
        assertThat(found.getDestinationGeneralAccountId()).isEqualTo(20L);
        assertThat(found.getStatus()).isEqualTo(AccountClosureStatus.REQUESTED);
    }

    @Test
    void selectByIdReturnsEmptyForUnknownClosure() {
        assertThat(accountClosureMapper.selectById(999L)).isEmpty();
    }

    private AccountClosureDTO insertClosureRequest() {
        AccountClosureDTO request = closureRequest(false);
        assertThat(accountClosureMapper.insertClosureRequest(request)).isOne();
        return request;
    }

    private AccountClosureDTO closureRequest(boolean earlyWithdrawalAgreed) {
        return AccountClosureDTO.builder()
                .accountId(ACCOUNT_ID)
                .destinationGeneralAccountId(20L)
                .earlyWithdrawalAgreed(earlyWithdrawalAgreed)
                .status(AccountClosureStatus.REQUESTED)
                .requestedAt(REQUESTED_AT)
                .build();
    }

    private String accountStatus() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery(
                                "SELECT status FROM account WHERE account_id = " + ACCOUNT_ID)) {
            resultSet.next();
            return resultSet.getString("status");
        }
    }

    private void updateAccountAmount(BigDecimal amount) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                "UPDATE account SET amount = ? WHERE account_id = ?")) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, ACCOUNT_ID);
            statement.executeUpdate();
        }
    }

    private static void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute(
                    """
                    CREATE TABLE customer (
                        customer_id BIGINT PRIMARY KEY,
                        name VARCHAR(50) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE account (
                        account_id BIGINT PRIMARY KEY,
                        customer_id BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        opened_at DATETIME NULL,
                        created_at DATETIME NOT NULL,
                        account_no VARCHAR(30) NULL,
                        limit_amount DECIMAL(15, 0) NOT NULL,
                        amount DECIMAL(15, 2) NOT NULL,
                        benefit VARCHAR(20) NULL
                    )
                    """);
            statement.execute(
                    """
                    INSERT INTO customer (customer_id, name)
                    VALUES (10, '조회 고객')
                    """);
            statement.execute(
                    """
                    CREATE TABLE account_closure_request (
                        closure_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        account_id BIGINT NOT NULL,
                        destination_general_account_id BIGINT NOT NULL,
                        early_withdrawal_agreed BOOLEAN NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        requested_at DATETIME NOT NULL,
                        processed_at DATETIME NULL,
                        processed_by BIGINT NULL,
                        rejection_reason VARCHAR(255) NULL,
                        withdrawal_id BIGINT NULL
                    )
                    """);
            statement.execute(
                    """
                    INSERT INTO account (
                        account_id, customer_id, status, opened_at, created_at,
                        account_no, limit_amount, amount, benefit
                    ) VALUES (
                        1, 10, 'OPENED', TIMESTAMP '2026-01-01 09:00:00',
                        TIMESTAMP '2025-12-01 09:00:00', '1234567890', 50000000, 1000, 'POSSIBLE'
                    )
                    """);
        }
    }
}
