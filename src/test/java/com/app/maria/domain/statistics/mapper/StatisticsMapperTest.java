package com.app.maria.domain.statistics.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.statistics.dto.AccountBenefitStatDTO;
import com.app.maria.domain.statistics.dto.AgeInvestmentStatDTO;
import com.app.maria.domain.statistics.dto.FxExchangeStatDTO;
import com.app.maria.domain.statistics.dto.ProductPurchaseStatDTO;
import com.app.maria.domain.statistics.dto.ReliefRateStatDTO;
import com.app.maria.domain.statistics.dto.StatisticsFilterDTO;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

class StatisticsMapperTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 8, 18);

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;
    private StatisticsMapper statisticsMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-statistics-test-config.xml")) {
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
        statisticsMapper = sqlSession.getMapper(StatisticsMapper.class);
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

    private StatisticsFilterDTO.StatisticsFilterDTOBuilder baseFilter() {
        return StatisticsFilterDTO.builder().referenceDate(REFERENCE_DATE);
    }

    // ---- selectAgeInvestmentStats ----

    @Test
    @DisplayName("나이대별로 매수금액(qty*avg_purchase_price)과 건수를 집계하고, SOLD_OUT/TERMINATED 잔고는 제외한다")
    void selectAgeInvestmentStatsAggregatesByAgeGroupExcludingSoldOutAndTerminated()
            throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1996, 1, 1)); // 30대
        insertCustomer(2L, "김철수", LocalDate.of(1966, 1, 1)); // 60대 이상
        insertAccount(1L, 1L, "1000000001", null);
        insertAccount(2L, 2L, "1000000002", null);
        insertDomesticProduct(100L, "005930", "삼성전자");
        insertDomesticStockBalance(1L, 100L, "10", "70000", "HOLDING");
        insertDomesticStockBalance(2L, 100L, "5", "70000", "HOLDING");
        insertDomesticStockBalance(1L, 100L, "999", "70000", "SOLD_OUT");

        List<AgeInvestmentStatDTO> result =
                statisticsMapper.selectAgeInvestmentStats(baseFilter().build());

        assertThat(result).hasSize(2);
        AgeInvestmentStatDTO thirties =
                result.stream()
                        .filter(r -> r.getAgeGroup().equals("30대"))
                        .findFirst()
                        .orElseThrow();
        assertThat(thirties.getPurchaseAmount()).isEqualByComparingTo("700000");
        assertThat(thirties.getPurchaseCount()).isEqualTo(1);
        AgeInvestmentStatDTO sixties =
                result.stream()
                        .filter(r -> r.getAgeGroup().equals("60대 이상"))
                        .findFirst()
                        .orElseThrow();
        assertThat(sixties.getPurchaseAmount()).isEqualByComparingTo("350000");
    }

    @Test
    @DisplayName("productName 필터를 적용하면 이름/티커가 일치하는 종목의 매수 현황만 집계된다")
    void selectAgeInvestmentStatsFiltersByProductName() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1996, 1, 1));
        insertAccount(1L, 1L, "1000000001", null);
        insertDomesticProduct(100L, "005930", "삼성전자");
        insertDomesticProduct(200L, "000660", "SK하이닉스");
        insertDomesticStockBalance(1L, 100L, "10", "70000", "HOLDING");
        insertDomesticStockBalance(1L, 200L, "10", "120000", "HOLDING");

        List<AgeInvestmentStatDTO> result =
                statisticsMapper.selectAgeInvestmentStats(baseFilter().productName("삼성").build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseAmount()).isEqualByComparingTo("700000");
    }

    @Test
    @DisplayName("매수 잔고가 없으면 빈 목록을 반환한다")
    void selectAgeInvestmentStatsReturnsEmptyListWhenNoBalanceExists() {
        List<AgeInvestmentStatDTO> result =
                statisticsMapper.selectAgeInvestmentStats(baseFilter().build());

        assertThat(result).isEmpty();
    }

    // ---- selectProductPurchaseStats ----

    @Test
    @DisplayName("종목별로 매수금액을 집계해 금액 내림차순으로 반환한다")
    void selectProductPurchaseStatsAggregatesByProductOrderedByAmountDescending()
            throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1000000001", null);
        insertDomesticProduct(100L, "005930", "삼성전자");
        insertDomesticProduct(200L, "000660", "SK하이닉스");
        insertDomesticStockBalance(1L, 100L, "10", "70000", "HOLDING"); // 700,000
        insertDomesticStockBalance(1L, 200L, "20", "120000", "HOLDING"); // 2,400,000

        List<ProductPurchaseStatDTO> result =
                statisticsMapper.selectProductPurchaseStats(baseFilter().build());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTicker()).isEqualTo("000660");
        assertThat(result.get(0).getPurchaseAmount()).isEqualByComparingTo("2400000");
        assertThat(result.get(1).getTicker()).isEqualTo("005930");
    }

    @Test
    @DisplayName("keyword가 계좌번호에 부분일치하면 해당 계좌의 매수 현황만 집계된다")
    void selectProductPurchaseStatsFiltersByKeywordMatchingAccountNo() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertCustomer(2L, "김철수", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1111111111", null);
        insertAccount(2L, 2L, "2222222222", null);
        insertDomesticProduct(100L, "005930", "삼성전자");
        insertDomesticStockBalance(1L, 100L, "10", "70000", "HOLDING");
        insertDomesticStockBalance(2L, 100L, "5", "70000", "HOLDING");

        List<ProductPurchaseStatDTO> result =
                statisticsMapper.selectProductPurchaseStats(baseFilter().keyword("1111").build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseAmount()).isEqualByComparingTo("700000");
    }

    @Test
    @DisplayName("keyword가 고객명에 부분일치해도 매칭된다 (계좌번호/고객명 통합검색, OR 조건)")
    void selectProductPurchaseStatsFiltersByKeywordMatchingCustomerName() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertCustomer(2L, "김철수", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1111111111", null);
        insertAccount(2L, 2L, "2222222222", null);
        insertDomesticProduct(100L, "005930", "삼성전자");
        insertDomesticStockBalance(1L, 100L, "10", "70000", "HOLDING");
        insertDomesticStockBalance(2L, 100L, "5", "70000", "HOLDING");

        List<ProductPurchaseStatDTO> result =
                statisticsMapper.selectProductPurchaseStats(baseFilter().keyword("홍길동").build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseAmount()).isEqualByComparingTo("700000");
    }

    // ---- selectFxExchangeStats ----

    @Test
    @DisplayName("확정일(final_at) 기준 날짜별로 가환전액/확정환전액 합계를 집계한다")
    void selectFxExchangeStatsAggregatesByFinalizedDate() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1000000001", null);
        insertKrwExchange(1L, "1000000", "2026-08-01 09:00:00", "990000", "2026-08-02 09:00:00");
        insertKrwExchange(1L, "500000", "2026-08-02 09:00:00", "495000", "2026-08-02 15:00:00");

        List<FxExchangeStatDTO> result =
                statisticsMapper.selectFxExchangeStats(baseFilter().build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.get(0).getProvisionalAmount()).isEqualByComparingTo("1500000");
        assertThat(result.get(0).getFinalAmount()).isEqualByComparingTo("1485000");
    }

    @Test
    @DisplayName("기간 필터 범위 밖의 환전 건은 제외된다")
    void selectFxExchangeStatsExcludesRowsOutsideDateRange() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1000000001", null);
        insertKrwExchange(1L, "1000000", "2026-08-01 09:00:00", "990000", "2026-08-01 09:00:00");
        insertKrwExchange(1L, "500000", "2026-08-20 09:00:00", "495000", "2026-08-20 09:00:00");

        List<FxExchangeStatDTO> result =
                statisticsMapper.selectFxExchangeStats(
                        baseFilter()
                                .startDateTime(java.time.LocalDateTime.of(2026, 8, 1, 0, 0))
                                .endDateTime(java.time.LocalDateTime.of(2026, 8, 10, 23, 59, 59))
                                .build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatDate()).isEqualTo(LocalDate.of(2026, 8, 1));
    }

    // ---- selectAccountBenefitStats ----

    @Test
    @DisplayName("세제혜택 상태별로 계좌 수를 집계하고, benefit이 null인 계좌는 UNCLASSIFIED로 묶는다")
    void selectAccountBenefitStatsGroupsNullBenefitAsUnclassified() throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertCustomer(2L, "김철수", LocalDate.of(1990, 1, 1));
        insertCustomer(3L, "이영희", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1000000001", "POSSIBLE");
        insertAccount(2L, 2L, "1000000002", "POSSIBLE");
        insertAccount(3L, 3L, "1000000003", null);

        List<AccountBenefitStatDTO> result =
                statisticsMapper.selectAccountBenefitStats(baseFilter().build());

        assertThat(result).hasSize(2);
        assertThat(result)
                .filteredOn(r -> r.getBenefit().equals("POSSIBLE"))
                .extracting(AccountBenefitStatDTO::getAccountCount)
                .containsExactly(2);
        assertThat(result)
                .filteredOn(r -> r.getBenefit().equals("UNCLASSIFIED"))
                .extracting(AccountBenefitStatDTO::getAccountCount)
                .containsExactly(1);
    }

    // ---- selectReliefRateStats ----

    @Test
    @DisplayName(
            "매도월 기준으로 감면율 구간을 나누고, 확정산 완료 건은 final_amount를 매도금액으로 사용하며, 데이터 없는 구간도 0으로 항상 3개 다 반환한다")
    void selectReliefRateStatsAlwaysReturnsThreeFixedBucketsWithZeroForEmptyOnes()
            throws SQLException {
        insertCustomer(1L, "홍길동", LocalDate.of(1990, 1, 1));
        insertAccount(1L, 1L, "1000000001", null);
        Long marchOrder = insertSellOrder(1L, "10", "100000", "2026-03-15 10:00:00", "EXECUTED");
        insertKrwExchangeForOrder(marchOrder, "990000", "FINALIZED");
        insertSellOrder(1L, "5", "100000", "2026-06-15 10:00:00", "EXECUTED");
        insertSellOrder(1L, "1", "100000", "2026-09-15 10:00:00", "REJECTED");

        List<ReliefRateStatDTO> result =
                statisticsMapper.selectReliefRateStats(baseFilter().build());

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(ReliefRateStatDTO::getPeriodLabel)
                .containsExactly("1~5월 (100%)", "6~7월 (80%)", "8~12월 (50%)");
        assertThat(result.get(0).getSellAmount()).isEqualByComparingTo("990000");
        assertThat(result.get(1).getSellAmount()).isEqualByComparingTo("500000");
        assertThat(result.get(2).getSellAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("매도 이력이 없어도 3개 구간을 전부 0원으로 반환한다")
    void selectReliefRateStatsReturnsThreeZeroBucketsWhenNoSellOrdersExist() {
        List<ReliefRateStatDTO> result =
                statisticsMapper.selectReliefRateStats(baseFilter().build());

        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(r -> assertThat(r.getSellAmount()).isEqualByComparingTo("0"));
    }

    // ---- schema / fixtures ----

    private void insertCustomer(Long customerId, String name, LocalDate birthDate)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "INSERT INTO customer (customer_id, name, birth_date) VALUES (%d, '%s', '%s')"
                            .formatted(customerId, name, birthDate));
        }
    }

    private void insertAccount(Long accountId, Long customerId, String accountNo, String benefit)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "INSERT INTO account (account_id, customer_id, account_no, benefit) VALUES (%d, %d, '%s', %s)"
                            .formatted(
                                    accountId,
                                    customerId,
                                    accountNo,
                                    benefit == null ? "NULL" : "'" + benefit + "'"));
        }
    }

    private void insertDomesticProduct(Long productId, String ticker, String name)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "INSERT INTO domestic_product (domestic_product_id, ticker, name) VALUES (%d, '%s', '%s')"
                            .formatted(productId, ticker, name));
        }
    }

    private void insertDomesticStockBalance(
            Long accountId, Long productId, String qty, String avgPurchasePrice, String status)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO domestic_stock_balance
                        (account_id, domestic_product_id, qty, avg_purchase_price, status, last_purchase_date)
                    VALUES (%d, %d, %s, %s, '%s', '2026-08-01 10:00:00')
                    """
                            .formatted(accountId, productId, qty, avgPurchasePrice, status));
        }
    }

    private void insertKrwExchange(
            Long accountId,
            String provisionalAmount,
            String provisionalAt,
            String finalAmount,
            String finalAt)
            throws SQLException {
        Long orderId = insertSellOrder(accountId, "1", "1000", provisionalAt, "EXECUTED");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO krw_exchange
                        (account_id, order_id, provisional_amount, provisional_at, final_amount, final_at, settlement_status)
                    VALUES (%d, %d, %s, '%s', %s, '%s', 'FINALIZED')
                    """
                            .formatted(
                                    accountId,
                                    orderId,
                                    provisionalAmount,
                                    provisionalAt,
                                    finalAmount,
                                    finalAt));
        }
    }

    private void insertKrwExchangeForOrder(
            Long orderId, String finalAmount, String settlementStatus) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    INSERT INTO krw_exchange
                        (account_id, order_id, provisional_amount, provisional_at, final_amount, final_at, settlement_status)
                    SELECT account_id, order_id, %s, processed_at, %s, processed_at, '%s'
                    FROM sell_order WHERE order_id = %d
                    """
                            .formatted(finalAmount, finalAmount, settlementStatus, orderId));
        }
    }

    private Long insertSellOrder(
            Long accountId, String sellQty, String basePrice, String processedAt, String status)
            throws SQLException {
        String sql =
                "INSERT INTO sell_order (account_id, sell_qty, base_price, processed_at, status) "
                        + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                java.sql.PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, accountId);
            statement.setBigDecimal(2, new java.math.BigDecimal(sellQty));
            statement.setBigDecimal(3, new java.math.BigDecimal(basePrice));
            statement.setString(4, processedAt);
            statement.setString(5, status);
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
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
                        name VARCHAR(50) NOT NULL,
                        birth_date DATE NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE account (
                        account_id BIGINT PRIMARY KEY,
                        customer_id BIGINT NOT NULL,
                        account_no VARCHAR(10),
                        benefit VARCHAR(12) NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE domestic_product (
                        domestic_product_id BIGINT PRIMARY KEY,
                        ticker VARCHAR(20) NOT NULL,
                        name VARCHAR(100) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE domestic_stock_balance (
                        domestic_stock_balance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        domestic_product_id BIGINT NOT NULL,
                        qty DECIMAL(15,4) NOT NULL,
                        avg_purchase_price DECIMAL(15,4) NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        last_purchase_date DATETIME NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE sell_order (
                        order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        sell_qty DECIMAL(15,4) NOT NULL,
                        base_price DECIMAL(15,4) NOT NULL,
                        processed_at DATETIME NULL,
                        status VARCHAR(10) NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE krw_exchange (
                        exchange_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        account_id BIGINT NOT NULL,
                        order_id BIGINT NOT NULL,
                        provisional_amount DECIMAL(15,2) NULL,
                        provisional_at DATETIME NULL,
                        final_amount DECIMAL(15,0) NULL,
                        final_at DATETIME NULL,
                        settlement_status VARCHAR(12) NOT NULL
                    )
                    """);
        }
    }
}
