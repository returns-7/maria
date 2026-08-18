package com.app.maria.domain.domestic.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.domestic.dto.DomesticHoldingDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentListDTO;
import com.app.maria.domain.domestic.dto.DomesticInvestmentSearchDTO;
import com.app.maria.domain.domestic.type.DomesticStockStatus;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
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

class DomesticStockBalanceMapperTest {

    private static final LocalDateTime PURCHASE_DATE = LocalDateTime.of(2026, 3, 5, 9, 0);

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private DomesticStockBalanceMapper domesticStockBalanceMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader =
                Resources.getResourceAsReader("mybatis-domesticstockbalance-test-config.xml")) {
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
        domesticStockBalanceMapper = sqlSession.getMapper(DomesticStockBalanceMapper.class);
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

    // ---- selectAccountSummaries ----

    @Test
    @DisplayName("보유종목이 없는 계좌도 예탁금 정보를 담아 목록에 나온다 (LEFT JOIN)")
    void selectAccountSummariesIncludesAccountsWithNoHoldings() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "1000000", "OPENED");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("홍길동");
        assertThat(result.get(0).getCashAmount()).isEqualByComparingTo("1000000");
        assertThat(result.get(0).getHoldingCount()).isEqualTo(0);
        assertThat(result.get(0).isHasRestrictedHolding()).isFalse();
    }

    @Test
    @DisplayName("holdingCount는 전량매도·보유종료 상태를 제외하고 센다")
    void selectAccountSummariesCountsOnlyActiveHoldings() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "0", "OPENED");
        insertDomesticProduct(1L, "005930", "삼성전자", "STOCK", null, null);
        insertDomesticProduct(2L, "000660", "SK하이닉스", "STOCK", null, null);
        insertDomesticProduct(3L, "035420", "NAVER", "STOCK", null, null);
        insertBalance(1L, 1L, "HOLDING", "10");
        insertBalance(1L, 2L, "SOLD_OUT", "0");
        insertBalance(1L, 3L, "TERMINATED", "0");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHoldingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("거래제한/거래정지 보유종목이 하나라도 있으면 hasRestrictedHolding이 true다")
    void selectAccountSummariesFlagsRestrictedHolding() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "0", "OPENED");
        insertDomesticProduct(1L, "005930", "삼성전자", "STOCK", null, null);
        insertDomesticProduct(2L, "000660", "SK하이닉스", "STOCK", null, null);
        insertBalance(1L, 1L, "HOLDING", "10");
        insertBalance(1L, 2L, "TRADE_SUSPENDED", "5");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 0, 20));

        assertThat(result.get(0).isHasRestrictedHolding()).isTrue();
    }

    @Test
    @DisplayName("customerName 필터는 고객명에 부분일치하는 계좌만 조회한다")
    void selectAccountSummariesFiltersByCustomerNameContains() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertCustomer(2L, "김철수");
        insertAccount(1L, 1L, "1111111111", "0", "OPENED");
        insertAccount(2L, 2L, "2222222222", "0", "OPENED");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(condition("홍길동", null, 0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("status 필터는 그 상태의 보유종목을 가진 계좌만 조회한다")
    void selectAccountSummariesFiltersByStatus() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertCustomer(2L, "김철수");
        insertAccount(1L, 1L, "1111111111", "0", "OPENED");
        insertAccount(2L, 2L, "2222222222", "0", "OPENED");
        insertDomesticProduct(1L, "005930", "삼성전자", "STOCK", null, null);
        insertBalance(1L, 1L, "TRADE_RESTRICTED", "10");
        insertBalance(2L, 1L, "HOLDING", "10");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(
                        condition(null, DomesticStockStatus.TRADE_RESTRICTED, 0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("OPENED 상태가 아닌 계좌는 목록에서 제외한다")
    void selectAccountSummariesExcludesNonOpenedAccounts() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertCustomer(2L, "김철수");
        insertAccount(1L, 1L, "1111111111", "0", "OPENED");
        insertAccount(2L, 2L, "2222222222", "0", "CLOSED");

        List<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("offset·size로 페이지네이션이 적용된다")
    void selectAccountSummariesAppliesOffsetAndSizeForPagination() throws SQLException {
        insertCustomer(1L, "고객1");
        insertCustomer(2L, "고객2");
        insertCustomer(3L, "고객3");
        insertAccount(1L, 1L, "1111111111", "0", "OPENED");
        insertAccount(2L, 2L, "2222222222", "0", "OPENED");
        insertAccount(3L, 3L, "3333333333", "0", "OPENED");

        List<DomesticInvestmentListDTO> firstPage =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 0, 2));
        List<DomesticInvestmentListDTO> secondPage =
                domesticStockBalanceMapper.selectAccountSummaries(condition(null, null, 2, 2));

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(1);
    }

    // ---- countAccountSummaries ----

    @Test
    @DisplayName("countAccountSummaries는 필터가 적용된 계좌 수를 반환한다")
    void countAccountSummariesMatchesFilteredRowCount() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertCustomer(2L, "김철수");
        insertAccount(1L, 1L, "1111111111", "0", "OPENED");
        insertAccount(2L, 2L, "2222222222", "0", "OPENED");

        int count = domesticStockBalanceMapper.countAccountSummaries(condition("홍길동", null, 0, 20));

        assertThat(count).isEqualTo(1);
    }

    // ---- selectAccountSummaryById ----

    @Test
    @DisplayName("accountId로 조회하면 해당 계좌의 요약 정보를 반환한다")
    void selectAccountSummaryByIdReturnsSummary() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "500000", "OPENED");

        Optional<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaryById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAccountNo()).isEqualTo("1234567890");
        assertThat(result.get().getCashAmount()).isEqualByComparingTo("500000");
    }

    @Test
    @DisplayName("존재하지 않는 accountId는 빈 Optional을 반환한다")
    void selectAccountSummaryByIdReturnsEmptyWhenNotFound() {
        Optional<DomesticInvestmentListDTO> result =
                domesticStockBalanceMapper.selectAccountSummaryById(999L);

        assertThat(result).isEmpty();
    }

    // ---- selectHoldingsByAccountId ----

    @Test
    @DisplayName("보유종목을 종목 정보와 함께 최근매수일 최신순으로 반환한다")
    void selectHoldingsByAccountIdReturnsHoldingsOrderedByLastPurchaseDateDesc()
            throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "0", "OPENED");
        insertDomesticProduct(1L, "448630", "TIGER 미국배당다우존스", "FUND", "85.50", "2023-05-10");
        insertDomesticProduct(2L, "005930", "삼성전자", "STOCK", null, null);
        insertBalanceWithDate(1L, 1L, "HOLDING", "10", PURCHASE_DATE);
        insertBalanceWithDate(1L, 2L, "HOLDING", "20", PURCHASE_DATE.plusDays(1));

        List<DomesticHoldingDTO> result = domesticStockBalanceMapper.selectHoldingsByAccountId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTicker()).isEqualTo("005930");
        assertThat(result.get(1).getTicker()).isEqualTo("448630");
        assertThat(result.get(1).getDomesticStockRatio()).isEqualByComparingTo("85.50");
    }

    @Test
    @DisplayName("보유종목이 없으면 빈 목록을 반환한다")
    void selectHoldingsByAccountIdReturnsEmptyListWhenNoHoldings() throws SQLException {
        insertCustomer(1L, "홍길동");
        insertAccount(1L, 1L, "1234567890", "0", "OPENED");

        List<DomesticHoldingDTO> result = domesticStockBalanceMapper.selectHoldingsByAccountId(1L);

        assertThat(result).isEmpty();
    }

    private DomesticInvestmentSearchDTO condition(
            String customerName, DomesticStockStatus status, int offset, int size) {
        return DomesticInvestmentSearchDTO.builder()
                .customerName(customerName)
                .status(status)
                .offset(offset)
                .size(size)
                .build();
    }

    private void insertCustomer(Long customerId, String name) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "INSERT INTO customer (customer_id, name) VALUES (%d, '%s')"
                            .formatted(customerId, name));
        }
    }

    private void insertAccount(
            Long accountId, Long customerId, String accountNo, String amount, String status)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO account (account_id, customer_id, account_no, amount, status)
                    VALUES (%d, %d, '%s', %s, '%s')
                    """
                            .formatted(accountId, customerId, accountNo, amount, status));
        }
    }

    private void insertDomesticProduct(
            Long id, String ticker, String name, String type, String ratio, String inceptionDate)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            String ratioValue = ratio == null ? "NULL" : "'" + ratio + "'";
            String inceptionValue = inceptionDate == null ? "NULL" : "'" + inceptionDate + "'";
            statement.execute(
                    """
                    INSERT INTO domestic_product
                    (domestic_product_id, ticker, name, market, type, domestic_stock_ratio, inception_date)
                    VALUES (%d, '%s', '%s', 'KOSPI', '%s', %s, %s)
                    """
                            .formatted(id, ticker, name, type, ratioValue, inceptionValue));
        }
    }

    private void insertBalance(Long accountId, Long productId, String status, String qty)
            throws SQLException {
        insertBalanceWithDate(accountId, productId, status, qty, PURCHASE_DATE);
    }

    private void insertBalanceWithDate(
            Long accountId, Long productId, String status, String qty, LocalDateTime purchaseDate)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO domestic_stock_balance
                    (account_id, domestic_product_id, qty, status, last_purchase_date, avg_purchase_price)
                    VALUES (%d, %d, %s, '%s', '%s', 100)
                    """
                            .formatted(accountId, productId, qty, status, purchaseDate));
        }
    }

    private void resetSchema() throws SQLException {
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
                        account_no VARCHAR(10),
                        amount DECIMAL(15, 4) NOT NULL DEFAULT 0,
                        status VARCHAR(20) NOT NULL DEFAULT 'OPENED'
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE domestic_product (
                        domestic_product_id BIGINT PRIMARY KEY,
                        ticker VARCHAR(20) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        market VARCHAR(50),
                        type VARCHAR(15) NOT NULL,
                        domestic_stock_ratio DECIMAL(5,2),
                        inception_date DATE
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE domestic_stock_balance (
                        domestic_stock_balance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        domestic_product_id BIGINT NOT NULL,
                        qty DECIMAL(15, 4) NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        last_purchase_date DATETIME NOT NULL,
                        avg_purchase_price DECIMAL(15, 4) NOT NULL
                    )
                    """);
        }
    }
}
