package com.app.maria.scenario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.exception.InvalidAccountRequestException;
import com.app.maria.domain.account.mapper.AccountMapper;
import com.app.maria.domain.account.service.AccountService;
import com.app.maria.domain.externaltradesync.service.ExternalTradeSyncService;
import com.app.maria.domain.inbound.dto.request.InboundRequestDTO;
import com.app.maria.domain.inbound.service.InboundService;
import com.app.maria.domain.sellorder.dto.request.SellOrderRequestDTO;
import com.app.maria.domain.sellorder.service.SellOrderService;
import com.app.maria.domain.settlement.dto.SettlementBatchDTO;
import com.app.maria.domain.settlement.provider.ExchangeRateProvider;
import com.app.maria.domain.settlement.service.SettlementService;
import com.app.maria.domain.settlement.type.BatchStatus;
import com.app.maria.domain.tax.batch.TaxSnapshotJobLauncher;
import com.app.maria.global.client.exchange.ExchangeRateClient;
import com.app.maria.global.client.kis.KisPriceClient;
import com.app.maria.global.client.mydata.MydataClient;
import com.app.maria.global.clock.dto.request.SystemClockChangeRequestDTO;
import com.app.maria.global.clock.service.BusinessClockService;
import com.app.maria.global.clock.service.SystemClockManagementService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 축소 규모(500명) 골든 시나리오 생성기.
 *
 * <p>CLAUDE.md §12 원칙에 따라 계산 데이터(account/inbound/sell_order/tax_snapshot 등)를 랜덤 INSERT가 아니라 실제 서비스
 * 계층 호출로 만든다. 계좌 신청(APPLIED)만 예외적으로 raw insert — 관리자 화면 관점에서 검증할 로직이 없는 구간(고객 채널이 아직 없어 신청 자체는 관리자
 * 서비스가 대리 처리할 뿐, 이 프로젝트엔 신청 vs 처리 2단계가 없음)이라 팀 논의로 SQL 시드로 남김.
 *
 * <p>사전 조건:
 *
 * <ul>
 *   <li>ria_admin DB에 customer가 정적 시드로 이미 로드돼 있어야 함 (계산 데이터 테이블은 비어 있어야 함)
 *   <li>return-securities(포트 10001)가 같은 population/PEPPER로 만든 securities DB로 떠 있어야 함
 * </ul>
 *
 * <p>securities DB 접속 정보(URL/계정/비밀번호)는 팀원마다 로컬 컨테이너 설정이 달라 하드코딩하지 않는다. 기본값은 {@code
 * jdbc:mariadb://localhost:3306/securities} / {@code root} / {@code secret}이고, 다르면 {@code
 * -Dsecurities.jdbc.url=...}, {@code -Dsecurities.jdbc.username=...}, {@code
 * -Dsecurities.jdbc.password=...}로 덮어쓸 것.
 *
 * <p>실행: {@code ./gradlew test --tests "com.app.maria.scenario.GoldenScenarioTest"
 * -PrunIntegration}. 재실행하려면 먼저
 * account/inbound/sell_order/krw_exchange/withdrawal*·settlement_*·tax_snapshot·tax_calculation을
 * TRUNCATE할 것 (customer_id UNIQUE 제약 때문에 재실행 시 계좌 중복 신청으로 실패함).
 *
 * <p>{@code -PrunIntegration} 없이 일반 {@code ./gradlew test}(CI 포함)를 돌리면 이 클래스는 제외된다 — 실제 DB/외부
 * 서비스(securities:10001)가 떠 있어야만 통과하는 데이터 생성기라 일반 빌드에 끼면 안 되기 때문.
 */
@Tag("integration")
@SpringBootTest
class GoldenScenarioTest {

    private static final long RANDOM_SEED = 20260817L;
    private static final int SCENARIO_SIZE = 500;
    private static final Long ADMIN_ID = 5L;

    // 팀원마다 로컬 컨테이너명/비밀번호가 다를 수 있어 하드코딩하지 않는다.
    // 필요하면 환경변수(SECURITIES_JDBC_URL 등)나 -D시스템프로퍼티로 덮어쓸 것.
    @Value("${securities.jdbc.url:jdbc:mariadb://localhost:3306/securities}")
    private String securitiesJdbcUrl;

    @Value("${securities.jdbc.username:root}")
    private String securitiesUser;

    @Value("${securities.jdbc.password:secret}")
    private String securitiesPassword;

    @Autowired private AccountMapper accountMapper;
    @Autowired private AccountService accountService;
    @Autowired private InboundService inboundService;
    @Autowired private SellOrderService sellOrderService;
    @Autowired private SettlementService settlementService;
    @Autowired private SystemClockManagementService systemClockManagementService;
    @Autowired private BusinessClockService businessClockService;
    @Autowired private TaxSnapshotJobLauncher taxSnapshotJobLauncher;
    @Autowired private ExternalTradeSyncService externalTradeSyncService;
    @Autowired private JdbcTemplate jdbcTemplate;

    // 실제 외부 API(KIS 시세·수출입은행 환율)만 스텁 처리 — 나머지(계좌 상태전이·FIFO 매도·한도체크·
    // 3-way MIN 입고·가환전·확정산·세액계산)는 전부 진짜 서비스 로직을 태운다.
    @MockitoBean(name = "exchangeRateClient")
    private ExchangeRateClient exchangeRateClient;

    @MockitoBean private KisPriceClient kisPriceClient;
    @MockitoBean private ExchangeRateProvider exchangeRateProvider;

    // 매도한도(누적 매도금액) 체크용 — 아직 검증 대상 아님(§8-3), 항상 0(전액 여유)으로 고정.
    // 계좌 승인 시 5천만원 합산한도 판정(MydataProvider)은 mock 안 함 — 실제 mydata_ria_account
    // 데이터로 판정돼서, 타사 RIA 한도가 이미 많이 찬 계좌는 정말로 개설이 막힌다.
    @MockitoBean private MydataClient mydataClient;

    private final Random random = new Random(RANDOM_SEED);

    @BeforeEach
    void setUpActorAndExternalStubs() {
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        ADMIN_ID, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(kisPriceClient.getPreviousClose(anyString(), anyString()))
                .thenAnswer(invocation -> BigDecimal.valueOf(50 + random.nextInt(2950)));
        when(exchangeRateClient.getBaseRate(anyString()))
                .thenAnswer(invocation -> BigDecimal.valueOf(1300 + random.nextInt(100)));
        when(exchangeRateProvider.getFinalRate(anyString(), any()))
                .thenAnswer(invocation -> BigDecimal.valueOf(1300 + random.nextInt(100)));
        when(mydataClient.getExternalSellTotal(anyString())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void generateGoldenScenario() throws Exception {
        List<CustomerRow> customers = loadCustomers(0, SCENARIO_SIZE);
        Counters counters = new Counters();

        try (Connection securitiesConnection = openSecuritiesConnection()) {
            for (CustomerRow customer : customers) {
                // 계좌마다 신청~매도 시점을 2026년 안에서 흩뿌린다 — §3 구간가중치(1~5월/6~7월/8~12월)도
                // 자연히 다양해지고, 산출된 일자가 전부 똑같아 보이는 문제도 없어진다.
                processCustomer(customer, randomBusinessDateTime(), counters, securitiesConnection);
            }
        }

        setClock(LocalDateTime.of(2026, 8, 17, 9, 0)); // 정산일로 이동 — 전날 가환전분만 확정산 대상
        SettlementBatchDTO batch = settlementService.executeSettlementBatchByAdmin();
        awaitBatchCompletion(batch.getBatchId());

        // 외부(타 금융기관) 순매수 동기화 — 개설 계좌 전원(mydata_trade에 이미 시드된 실제 거래 사용).
        // 이게 있어야 조정비율([3]단계)이 100%로만 고정되지 않고 계좌마다 달라짐.
        externalTradeSyncService.syncAll();

        taxSnapshotJobLauncher.launch(businessClockService.now());

        System.out.printf(
                "골든 시나리오 완료 — 신청 %d / 승인 %d / 반려 %d / 한도초과 %d / 입고 %d / 매도 %d%n",
                counters.applied,
                counters.approved,
                counters.rejected,
                counters.limitExceeded,
                counters.inbounded,
                counters.sold);
    }

    /**
     * 검증용: 기존 500명은 건드리지 않고 새 계좌 10개(501~510)만 추가로 매도·정산까지 처리한다. 세액 스냅샷 배치는 일부러 안 돌린다 — 관리자 화면에서
     * "배치 수동 실행"을 직접 눌러서, 실행 전/후로 이 계좌들이 새로 나타나는지(=배치가 실제로 최신 데이터를 반영하는지) 눈으로 확인하기 위함.
     */
    @Test
    void addFreshAccountsAfterBatch() throws Exception {
        List<CustomerRow> customers = loadCustomers(SCENARIO_SIZE, 10);
        Counters counters = new Counters();

        try (Connection securitiesConnection = openSecuritiesConnection()) {
            for (CustomerRow customer : customers) {
                processCustomer(
                        customer,
                        LocalDateTime.of(2026, 8, 17, 10, 0),
                        counters,
                        securitiesConnection);
            }
        }

        // 8/17 정산배치는 앞선 generateGoldenScenario()에서 이미 한 번 돌았다(업무일당 1회, §8-4 멱등).
        // 새 매도분을 확정산 대상에 포함시키려면 다음 업무일(8/18)로 넘어가야 한다.
        setClock(LocalDateTime.of(2026, 8, 18, 9, 0));
        SettlementBatchDTO batch = settlementService.executeSettlementBatchByAdmin();
        awaitBatchCompletion(batch.getBatchId());
        externalTradeSyncService.syncAll();

        System.out.printf(
                "추가 계좌 완료 — 신청 %d / 승인 %d / 반려 %d / 한도초과 %d / 입고 %d / 매도 %d "
                        + "(세액 배치는 안 돌림 — 관리자 화면에서 수동 실행 버튼으로 확인할 것)%n",
                counters.applied,
                counters.approved,
                counters.rejected,
                counters.limitExceeded,
                counters.inbounded,
                counters.sold);
    }

    private void processCustomer(
            CustomerRow customer,
            LocalDateTime at,
            Counters counters,
            Connection securitiesConnection)
            throws SQLException {
        setClockRaw(at);

        Long accountId = applyAccount(customer.customerId());
        counters.applied++;

        if (random.nextInt(100) < 15) {
            accountService.rejectAccount(accountId, "심사 기준 미충족");
            counters.rejected++;
            return;
        }
        try {
            accountService.approveAccount(accountId);
        } catch (InvalidAccountRequestException e) {
            // 타 증권사 RIA 한도 합산 5천만원 초과(§2 규칙1) — 실제 mydata_ria_account 데이터 기반으로
            // 판정되므로, 이 계좌는 실제로 개설 불가능한 계좌다.
            counters.limitExceeded++;
            return;
        }
        counters.approved++;

        if (random.nextInt(100) >= 70) {
            return; // 개설만 하고 입고는 안 하는 계좌 30%
        }

        HoldingRow holding = pickHolding(customer.ciHash(), securitiesConnection);
        if (holding == null) {
            return;
        }
        inboundService.processInbound(
                InboundRequestDTO.builder()
                        .accountId(accountId)
                        .foreignProductId(holding.foreignProductId())
                        .requestedQty(holding.heldQty())
                        .currentHoldingAtRequest(holding.heldQty())
                        .build());
        counters.inbounded++;

        if (random.nextInt(100) >= 60) {
            return; // 입고만 하고 매도는 안 하는 계좌 40%
        }
        BigDecimal sellQty =
                holding.heldQty()
                        .multiply(BigDecimal.valueOf(30 + random.nextInt(70)))
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.DOWN);
        if (sellQty.signum() <= 0) {
            return;
        }
        sellOrderService.placeSellOrder(
                ADMIN_ID,
                SellOrderRequestDTO.builder()
                        .accountId(accountId)
                        .foreignProductId(holding.foreignProductId())
                        .sellQty(sellQty)
                        .build());
        counters.sold++;
    }

    private List<CustomerRow> loadCustomers(int offset, int limit) {
        return jdbcTemplate.query(
                "SELECT customer_id, ci_hash FROM customer ORDER BY customer_id LIMIT ?, ?",
                (rs, rowNum) -> new CustomerRow(rs.getLong("customer_id"), rs.getString("ci_hash")),
                offset,
                limit);
    }

    private static final class Counters {
        int applied, approved, rejected, limitExceeded, inbounded, sold;
    }

    private Long applyAccount(Long customerId) {
        BigDecimal limitAmount = BigDecimal.valueOf(5_000_000L + random.nextInt(35) * 1_000_000L);
        accountMapper.insertApplication(
                AccountDTO.builder()
                        .customerId(customerId)
                        .limitAmount(limitAmount)
                        .createdAt(businessClockService.now())
                        .build());
        return accountMapper.selectByCustomerId(customerId).orElseThrow().getAccountId();
    }

    private Connection openSecuritiesConnection() throws SQLException {
        return DriverManager.getConnection(securitiesJdbcUrl, securitiesUser, securitiesPassword);
    }

    // MariaDB의 ORDER BY RAND()는 Java 쪽 RANDOM_SEED로 통제가 안 돼 재현성이 깨진다 — 후보를 전부
    // 가져온 뒤 시드된 random으로 직접 골라야 "같은 시드 = 같은 결과"가 보장된다.
    private HoldingRow pickHolding(String ciHash, Connection connection) throws SQLException {
        String sql =
                "SELECT rs.foreign_product_id, rs.held_qty "
                        + "FROM registrable_stock rs "
                        + "JOIN general_account ga ON ga.general_account_id = rs.general_account_id "
                        + "JOIN general_customer gc ON gc.general_customer_id = ga.general_customer_id "
                        + "WHERE gc.ci_hash = ?";
        List<HoldingRow> candidates = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ciHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    candidates.add(
                            new HoldingRow(
                                    resultSet.getLong("foreign_product_id"),
                                    resultSet.getBigDecimal("held_qty")));
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    // 계좌마다 신청/매도 시점을 다르게 흩뿌리는 용도. 관리자가 조작한 게 아니라 시나리오 진행용
    // 시간 이동이라 감사로그(SystemClockManagementService.changeSystemTime) 없이 직접 갱신한다.
    // 2026-08-16까지만 씀 — 정산일(8/17)보다 하루라도 전이어야 익일정산 대상이 된다.
    private void setClockRaw(LocalDateTime newDatetime) {
        jdbcTemplate.update("UPDATE system_clock SET current_datetime = ?", newDatetime);
    }

    private LocalDateTime randomBusinessDateTime() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 9, 0);
        long maxDayOffset =
                java.time.temporal.ChronoUnit.DAYS.between(
                        start.toLocalDate(), LocalDateTime.of(2026, 8, 16, 9, 0).toLocalDate());
        return start.plusDays(random.nextInt((int) maxDayOffset + 1)).plusHours(random.nextInt(8));
    }

    private void setClock(LocalDateTime newDatetime) {
        systemClockManagementService.changeSystemTime(
                ADMIN_ID, new SystemClockChangeRequestDTO(newDatetime, "SCENARIO_TEST"));
    }

    private void awaitBatchCompletion(Long batchId) throws InterruptedException {
        for (int attempt = 0; attempt < 60; attempt++) {
            BatchStatus status = settlementService.getSettlementBatch(batchId).getStatus();
            if (status != BatchStatus.RUNNING) {
                return;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("정산 배치가 제한 시간 내 끝나지 않았습니다.");
    }

    private record CustomerRow(Long customerId, String ciHash) {}

    private record HoldingRow(Long foreignProductId, BigDecimal heldQty) {}
}
