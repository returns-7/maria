package com.app.maria.domain.foreignproduct.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.foreignproduct.dto.ForeignProductDTO;
import com.app.maria.domain.foreignproduct.type.ForeignProductType;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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

class ForeignProductMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private ForeignProductMapper foreignProductMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader =
                Resources.getResourceAsReader("mybatis-foreignproduct-test-config.xml")) {
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
        foreignProductMapper = sqlSession.getMapper(ForeignProductMapper.class);
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
    @DisplayName("등록된 종목 전체를 조회한다")
    void selectAllReturnsAllProducts() throws SQLException {
        insertProduct("AAPL", "애플", "NASDAQ", "USD", "FOREIGN_STOCK");
        insertProduct("TSLA", "테슬라", "NASDAQ", "USD", "FOREIGN_STOCK");

        List<ForeignProductDTO> result = foreignProductMapper.selectAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ForeignProductDTO::getTicker)
                .containsExactlyInAnyOrder("AAPL", "TSLA");
    }

    @Test
    @DisplayName("종목이 없으면 빈 리스트를 반환한다")
    void selectAllReturnsEmptyListWhenNoProductsExist() {
        List<ForeignProductDTO> result = foreignProductMapper.selectAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("foreign_product_id 오름차순으로 정렬되어 조회된다")
    void selectAllReturnsProductsOrderedById() throws SQLException {
        insertProduct("TSLA", "테슬라", "NASDAQ", "USD", "FOREIGN_STOCK");
        insertProduct("AAPL", "애플", "NASDAQ", "USD", "FOREIGN_STOCK");

        List<ForeignProductDTO> result = foreignProductMapper.selectAll();

        assertThat(result.get(0).getTicker()).isEqualTo("TSLA");
        assertThat(result.get(1).getTicker()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("모든 필드가 정확히 매핑되어 조회된다")
    void selectAllMapsAllFieldsCorrectly() throws SQLException {
        insertProduct("0700.HK", "텐센트", "HKEX", "HKD", "FOREIGN_STOCK");

        List<ForeignProductDTO> result = foreignProductMapper.selectAll();

        assertThat(result).hasSize(1);
        ForeignProductDTO dto = result.get(0);
        assertThat(dto.getForeignProductId()).isNotNull();
        assertThat(dto.getTicker()).isEqualTo("0700.HK");
        assertThat(dto.getName()).isEqualTo("텐센트");
        assertThat(dto.getMarket()).isEqualTo("HKEX");
        assertThat(dto.getCurrency()).isEqualTo("HKD");
        assertThat(dto.getForeignProductType()).isEqualTo(ForeignProductType.FOREIGN_STOCK);
    }

    @Test
    @DisplayName("foreign_product_id로 종목 단건을 조회한다")
    void selectByIdReturnsProductWhenExists() throws SQLException {
        insertProduct("AAPL", "애플", "NASDAQ", "USD", "FOREIGN_STOCK");
        Long id = foreignProductMapper.selectAll().get(0).getForeignProductId();

        Optional<ForeignProductDTO> result = foreignProductMapper.selectById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("AAPL");
        assertThat(result.get().getName()).isEqualTo("애플");
    }

    @Test
    @DisplayName("존재하지 않는 foreign_product_id로 조회하면 빈 Optional을 반환한다")
    void selectByIdReturnsEmptyWhenNotExists() {
        Optional<ForeignProductDTO> result = foreignProductMapper.selectById(999L);

        assertThat(result).isEmpty();
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute(
                    """
                    CREATE TABLE foreign_product (
                        foreign_product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        ticker VARCHAR(20) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        market VARCHAR(20) NOT NULL,
                        currency VARCHAR(10) NOT NULL,
                        type VARCHAR(20) NOT NULL
                    )
                    """);
        }
    }

    private void insertProduct(
            String ticker, String name, String market, String currency, String type)
            throws SQLException {
        String sql =
                "INSERT INTO foreign_product (ticker, name, market, currency, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ticker);
            statement.setString(2, name);
            statement.setString(3, market);
            statement.setString(4, currency);
            statement.setString(5, type);
            statement.executeUpdate();
        }
    }
}
