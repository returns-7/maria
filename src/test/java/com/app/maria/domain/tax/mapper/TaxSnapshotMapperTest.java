package com.app.maria.domain.tax.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.maria.domain.tax.dto.TaxSnapshotDTO;
import com.app.maria.domain.tax.fixture.TaxTestFixture;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
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

class TaxSnapshotMapperTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long OTHER_ACCOUNT_ID = 2L;
    private static final LocalDateTime CALCULATED_AT = LocalDateTime.of(2026, 8, 13, 2, 0);

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;
    private static TaxTestFixture fixture;

    private SqlSession sqlSession;
    private TaxSnapshotMapper taxSnapshotMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-tax-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource =
                (PooledDataSource)
                        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        fixture = new TaxTestFixture(dataSource);
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        fixture.resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        taxSnapshotMapper = sqlSession.getMapper(TaxSnapshotMapper.class);
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

    private TaxSnapshotDTO snapshot(Long accountId, LocalDateTime calculatedAt, String finalTax) {
        return TaxSnapshotDTO.builder()
                .accountId(accountId)
                .calculatedAt(calculatedAt)
                .weightedSell(new BigDecimal("43000000.00"))
                .originalGainAmount(new BigDecimal("32000000.00"))
                .weightedGain(new BigDecimal("27800000.00"))
                .weightedExternalAmount(new BigDecimal("11000000.00"))
                .adjustRatio(new BigDecimal("0.7442"))
                .finalDeduction(new BigDecimal("20688760.00"))
                .finalTax(new BigDecimal(finalTax))
                .build();
    }

    @Test
    @DisplayName("신규 계좌는 새 행으로 삽입되고 모든 컬럼이 그대로 들어간다")
    void upsert_신규삽입_컬럼매핑() {
        taxSnapshotMapper.upsertSnapshots(
                List.of(snapshot(ACCOUNT_ID, CALCULATED_AT, "1938472.80")));

        TaxSnapshotDTO saved = taxSnapshotMapper.selectByAccountId(ACCOUNT_ID).orElseThrow();
        assertThat(saved.getSnapshotId()).isNotNull();
        assertThat(saved.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getCalculatedAt()).isEqualTo(CALCULATED_AT);
        assertThat(saved.getWeightedSell()).isEqualByComparingTo("43000000.00");
        assertThat(saved.getOriginalGainAmount()).isEqualByComparingTo("32000000.00");
        assertThat(saved.getWeightedGain()).isEqualByComparingTo("27800000.00");
        assertThat(saved.getWeightedExternalAmount()).isEqualByComparingTo("11000000.00");
        assertThat(saved.getAdjustRatio()).isEqualByComparingTo("0.7442");
        assertThat(saved.getFinalDeduction()).isEqualByComparingTo("20688760.00");
        assertThat(saved.getFinalTax()).isEqualByComparingTo("1938472.80");
    }

    @Test
    @DisplayName("같은 계좌를 다시 넣으면 새 행이 아니라 기존 행을 덮어쓴다(idempotent)")
    void upsert_같은계좌_덮어쓴다() {
        taxSnapshotMapper.upsertSnapshots(
                List.of(snapshot(ACCOUNT_ID, LocalDateTime.of(2026, 8, 12, 2, 0), "100.00")));
        Long firstSnapshotId =
                taxSnapshotMapper.selectByAccountId(ACCOUNT_ID).orElseThrow().getSnapshotId();

        taxSnapshotMapper.upsertSnapshots(
                List.of(snapshot(ACCOUNT_ID, CALCULATED_AT, "999999.99")));

        TaxSnapshotDTO saved = taxSnapshotMapper.selectByAccountId(ACCOUNT_ID).orElseThrow();
        assertThat(saved.getSnapshotId()).isEqualTo(firstSnapshotId);
        assertThat(saved.getCalculatedAt()).isEqualTo(CALCULATED_AT);
        assertThat(saved.getFinalTax()).isEqualByComparingTo("999999.99");
    }

    @Test
    @DisplayName("같은 배치 안에 여러 계좌를 한 번에 넣을 수 있다")
    void upsert_여러계좌_한번에() {
        taxSnapshotMapper.upsertSnapshots(
                List.of(
                        snapshot(ACCOUNT_ID, CALCULATED_AT, "100.00"),
                        snapshot(OTHER_ACCOUNT_ID, CALCULATED_AT, "200.00")));

        assertThat(taxSnapshotMapper.selectByAccountId(ACCOUNT_ID).orElseThrow().getFinalTax())
                .isEqualByComparingTo("100.00");
        assertThat(
                        taxSnapshotMapper
                                .selectByAccountId(OTHER_ACCOUNT_ID)
                                .orElseThrow()
                                .getFinalTax())
                .isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("존재하지 않는 계좌를 조회하면 빈 Optional이다")
    void selectByAccountId_없으면_빈값() {
        assertThat(taxSnapshotMapper.selectByAccountId(999L)).isEmpty();
    }

    @Test
    @DisplayName("여러 계좌 id로 한 번에 조회하면 해당하는 스냅샷만 반환된다")
    void selectByAccountIds_해당계좌만() {
        taxSnapshotMapper.upsertSnapshots(
                List.of(
                        snapshot(ACCOUNT_ID, CALCULATED_AT, "100.00"),
                        snapshot(OTHER_ACCOUNT_ID, CALCULATED_AT, "200.00"),
                        snapshot(3L, CALCULATED_AT, "300.00")));

        List<TaxSnapshotDTO> result =
                taxSnapshotMapper.selectByAccountIds(List.of(ACCOUNT_ID, OTHER_ACCOUNT_ID));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TaxSnapshotDTO::getAccountId)
                .containsExactlyInAnyOrder(ACCOUNT_ID, OTHER_ACCOUNT_ID);
    }

    @Test
    @DisplayName("스냅샷이 없으면 selectByAccountIds는 빈 목록을 반환한다")
    void selectByAccountIds_없으면_빈목록() {
        assertThat(taxSnapshotMapper.selectByAccountIds(List.of(ACCOUNT_ID))).isEmpty();
    }
}
