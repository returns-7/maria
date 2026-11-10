package com.app.maria.domain.externaltradesync.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.externaltradesync.dto.ExternalTradeSyncCursorDTO;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

class ExternalTradeSyncCursorMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private ExternalTradeSyncCursorMapper cursorMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader =
                Resources.getResourceAsReader("mybatis-externaltradesync-test-config.xml")) {
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
        cursorMapper = sqlSession.getMapper(ExternalTradeSyncCursorMapper.class);
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
    @DisplayName("커서가 없는 고객을 조회하면 빈 값을 반환한다")
    void selectByCustomerIdReturnsEmptyWhenNoCursorExists() {
        Optional<ExternalTradeSyncCursorDTO> result = cursorMapper.selectByCustomerId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("처음 upsertCursor를 호출하면 새 행이 생성된다")
    void upsertCursorInsertsNewRowWhenNoneExists() {
        cursorMapper.upsertCursor(
                ExternalTradeSyncCursorDTO.builder()
                        .customerId(1L)
                        .lastSyncedTradeDate(LocalDate.of(2026, 3, 5))
                        .build());

        Optional<ExternalTradeSyncCursorDTO> result = cursorMapper.selectByCustomerId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo(1L);
        assertThat(result.get().getLastSyncedTradeDate()).isEqualTo(LocalDate.of(2026, 3, 5));
    }

    @Test
    @DisplayName("같은 고객으로 다시 upsertCursor를 호출하면 새 행을 만들지 않고 기존 값을 갱신한다")
    void upsertCursorUpdatesExistingRowInsteadOfInsertingDuplicate() throws SQLException {
        cursorMapper.upsertCursor(
                ExternalTradeSyncCursorDTO.builder()
                        .customerId(1L)
                        .lastSyncedTradeDate(LocalDate.of(2026, 3, 5))
                        .build());

        cursorMapper.upsertCursor(
                ExternalTradeSyncCursorDTO.builder()
                        .customerId(1L)
                        .lastSyncedTradeDate(LocalDate.of(2026, 4, 1))
                        .build());

        Optional<ExternalTradeSyncCursorDTO> result = cursorMapper.selectByCustomerId(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getLastSyncedTradeDate()).isEqualTo(LocalDate.of(2026, 4, 1));

        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery(
                                "SELECT COUNT(*) AS cnt FROM external_trade_sync_cursor WHERE customer_id = 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt("cnt")).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("서로 다른 고객의 커서는 독립적으로 저장된다")
    void upsertCursorKeepsCursorsIndependentPerCustomer() {
        cursorMapper.upsertCursor(
                ExternalTradeSyncCursorDTO.builder()
                        .customerId(1L)
                        .lastSyncedTradeDate(LocalDate.of(2026, 3, 5))
                        .build());
        cursorMapper.upsertCursor(
                ExternalTradeSyncCursorDTO.builder()
                        .customerId(2L)
                        .lastSyncedTradeDate(LocalDate.of(2026, 5, 20))
                        .build());

        assertThat(cursorMapper.selectByCustomerId(1L).get().getLastSyncedTradeDate())
                .isEqualTo(LocalDate.of(2026, 3, 5));
        assertThat(cursorMapper.selectByCustomerId(2L).get().getLastSyncedTradeDate())
                .isEqualTo(LocalDate.of(2026, 5, 20));
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute(
                    """
                    CREATE TABLE external_trade_sync_cursor (
                        cursor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        customer_id BIGINT NOT NULL,
                        last_synced_trade_date DATE,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_external_trade_sync_cursor__customer UNIQUE (customer_id)
                    )
                    """);
        }
    }
}
