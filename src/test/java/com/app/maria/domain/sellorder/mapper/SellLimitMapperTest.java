package com.app.maria.domain.sellorder.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
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

class SellLimitMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;
    private SellLimitMapper sellLimitMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-selllimit-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource =
                (PooledDataSource)
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        sellLimitMapper = sqlSession.getMapper(SellLimitMapper.class);
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

    // ---- selectAccountLimitForUpdate ----

    @Test
    @DisplayName("계좌의 limit_amount를 정확히 그 계좌 것으로 조회한다 (다른 계좌 값과 안 섞임)")
    void selectAccountLimitForUpdateReturnsThisAccountsLimitNotAnotherAccounts()
            throws SQLException {
        Long customerA = insertCustomer("ci-hash-a");
        Long customerB = insertCustomer("ci-hash-b");
        Long accountA = insertAccount(customerA, "30000000");
        insertAccount(customerB, "45000000");

        Optional<BigDecimal> result = sellLimitMapper.selectAccountLimitForUpdate(accountA);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo("30000000");
    }

    @Test
    @DisplayName("존재하지 않는 계좌면 빈 Optional을 반환한다")
    void selectAccountLimitForUpdateReturnsEmptyWhenAccountDoesNotExist() {
        Optional<BigDecimal> result = sellLimitMapper.selectAccountLimitForUpdate(999L);

        assertThat(result).isEmpty();
    }

    // ---- sumUsedAmount ----

    @Test
    @DisplayName("매도 주문이 하나도 없으면 null이 아니라 0을 반환한다")
    void sumUsedAmountReturnsZeroNotNullWhenNoOrdersExist() throws SQLException {
        Long customerId = insertCustomer("ci-hash-none");
        Long accountId = insertAccount(customerId, "10000000");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("FINALIZED 건은 확정환전액(final_amount)으로 잡는다")
    void sumUsedAmountUsesFinalAmountForFinalizedOrders() throws SQLException {
        Long customerId = insertCustomer("ci-hash-finalized");
        Long accountId = insertAccount(customerId, "10000000");
        Long orderId = insertSellOrder(accountId, "EXECUTED", "1000", "10");
        insertExchange(orderId, accountId, "9500", "9800", "FINALIZED");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isEqualByComparingTo("9800");
    }

    @Test
    @DisplayName("PROVISIONAL(가환전만 된) 건은 base_price*qty가 아니라 provisional_amount로 잡는다")
    void sumUsedAmountUsesProvisionalAmountForProvisionalOrders() throws SQLException {
        Long customerId = insertCustomer("ci-hash-provisional");
        Long accountId = insertAccount(customerId, "10000000");
        Long orderId = insertSellOrder(accountId, "EXECUTED", "1000", "10");
        insertExchange(orderId, accountId, "9900", null, "PROVISIONAL");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isEqualByComparingTo("9900");
    }

    @Test
    @DisplayName("환전 이력이 전혀 없는 주문은 base_price*qty로 잡는다")
    void sumUsedAmountUsesBasePriceTimesQtyWhenNoExchangeExists() throws SQLException {
        Long customerId = insertCustomer("ci-hash-pending");
        Long accountId = insertAccount(customerId, "10000000");
        insertSellOrder(accountId, "RECEIVED", "1000", "10");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("REJECTED 주문은 합계에서 제외한다")
    void sumUsedAmountExcludesRejectedOrders() throws SQLException {
        Long customerId = insertCustomer("ci-hash-rejected");
        Long accountId = insertAccount(customerId, "10000000");
        insertSellOrder(accountId, "REJECTED", "1000", "10");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("FINALIZED+PROVISIONAL+미확정 건을 각자 다른 산식으로 계산해 합산한다")
    void sumUsedAmountSumsMixedOrdersWithEachOwnFormula() throws SQLException {
        Long customerId = insertCustomer("ci-hash-mixed");
        Long accountId = insertAccount(customerId, "10000000");

        Long finalizedOrder = insertSellOrder(accountId, "EXECUTED", "1000", "10");
        insertExchange(finalizedOrder, accountId, "9500", "9800", "FINALIZED");

        Long provisionalOrder = insertSellOrder(accountId, "EXECUTED", "2000", "5");
        insertExchange(provisionalOrder, accountId, "9900", null, "PROVISIONAL");

        insertSellOrder(accountId, "RECEIVED", "500", "4");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountId);

        assertThat(result).isEqualByComparingTo("21700"); // 9800 + 9900 + (500*4)
    }

    @Test
    @DisplayName("다른 계좌의 매도 금액은 이 계좌 합계에 안 섞인다")
    void sumUsedAmountDoesNotLeakAmountsFromOtherAccounts() throws SQLException {
        Long customerA = insertCustomer("ci-hash-x");
        Long customerB = insertCustomer("ci-hash-y");
        Long accountA = insertAccount(customerA, "10000000");
        Long accountB = insertAccount(customerB, "10000000");
        insertSellOrder(accountA, "RECEIVED", "700", "1");
        insertSellOrder(accountB, "RECEIVED", "88888888", "1");

        BigDecimal result = sellLimitMapper.sumUsedAmount(accountA);

        assertThat(result).isEqualByComparingTo("700");
    }

    // ---- selectCiHashByAccountId ----

    @Test
    @DisplayName("account_id로 customer를 조인해 정확히 그 고객의 ci_hash를 반환한다")
    void selectCiHashByAccountIdReturnsMatchingCustomersCiHashNotAnothers() throws SQLException {
        Long customerA = insertCustomer("ci-hash-owner-a");
        Long customerB = insertCustomer("ci-hash-owner-b");
        Long accountA = insertAccount(customerA, "10000000");
        insertAccount(customerB, "10000000");

        Optional<String> result = sellLimitMapper.selectCiHashByAccountId(accountA);

        assertThat(result).contains("ci-hash-owner-a");
    }

    @Test
    @DisplayName("존재하지 않는 계좌면 빈 Optional을 반환한다")
    void selectCiHashByAccountIdReturnsEmptyWhenAccountDoesNotExist() {
        Optional<String> result = sellLimitMapper.selectCiHashByAccountId(999L);

        assertThat(result).isEmpty();
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute(
                    """
                    CREATE TABLE customer (
                        customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        ci_hash VARCHAR(64) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE account (
                        account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        customer_id BIGINT NOT NULL,
                        limit_amount DECIMAL(15,0) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE sell_order (
                        order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        status VARCHAR(12) NOT NULL,
                        base_price DECIMAL(15,4) NOT NULL,
                        sell_qty DECIMAL(15,4) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE krw_exchange (
                        exchange_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        order_id BIGINT NOT NULL,
                        provisional_amount DECIMAL(15,0),
                        final_amount DECIMAL(15,0),
                        settlement_status VARCHAR(12) NOT NULL
                    )
                    """);
        }
    }

    private Long insertCustomer(String ciHash) throws SQLException {
        String sql = "INSERT INTO customer (ci_hash) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ciHash);
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    private Long insertAccount(Long customerId, String limitAmount) throws SQLException {
        String sql = "INSERT INTO account (customer_id, limit_amount) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setBigDecimal(2, new BigDecimal(limitAmount));
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    private Long insertSellOrder(Long accountId, String status, String basePrice, String sellQty)
            throws SQLException {
        String sql =
                "INSERT INTO sell_order (account_id, status, base_price, sell_qty) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, accountId);
            statement.setString(2, status);
            statement.setBigDecimal(3, new BigDecimal(basePrice));
            statement.setBigDecimal(4, new BigDecimal(sellQty));
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    private void insertExchange(
            Long orderId,
            Long accountId,
            String provisionalAmount,
            String finalAmount,
            String settlementStatus)
            throws SQLException {
        String sql =
                "INSERT INTO krw_exchange (account_id, order_id, provisional_amount, final_amount, settlement_status) "
                        + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, orderId);
            statement.setBigDecimal(3, new BigDecimal(provisionalAmount));
            statement.setBigDecimal(4, finalAmount == null ? null : new BigDecimal(finalAmount));
            statement.setString(5, settlementStatus);
            statement.executeUpdate();
        }
    }
}
