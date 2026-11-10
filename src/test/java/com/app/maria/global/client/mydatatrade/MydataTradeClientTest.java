package com.app.maria.global.client.mydatatrade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.app.maria.domain.externaltradesync.dto.request.MydataTradeRequestDTO;
import com.app.maria.domain.externaltradesync.dto.response.MydataTradeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MydataTradeClientTest {

    private static final String TRADES_URL = "http://localhost:10002/api/mydata/trades";

    private MockRestServiceServer mockServer;
    private MydataTradeClient mydataTradeClient;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        RestClient.Builder builder =
                RestClient.builder()
                        .baseUrl("http://localhost:10002")
                        .messageConverters(
                                converters ->
                                        converters.add(
                                                0,
                                                new MappingJackson2HttpMessageConverter(
                                                        objectMapper)));
        mockServer = MockRestServiceServer.bindTo(builder).build();
        mydataTradeClient = new MydataTradeClient(builder.build());
    }

    @Test
    @DisplayName("정상 응답이면 거래 목록을 그대로 반환한다")
    void getTradesReturnsListOnSuccess() {
        mockServer
                .expect(requestTo(TRADES_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withSuccess(
                                """
                        {
                          "message": "조회 성공",
                          "data": [
                            {
                              "tradeId": 100,
                              "ciHash": "ci-1",
                              "brokerName": "증권사A",
                              "tradeType": "BUY",
                              "stockType": "FOREIGN_STOCK",
                              "qty": 10,
                              "tradeDate": "2026-03-05",
                              "amount": 1000000,
                              "fundCode": null
                            },
                            {
                              "tradeId": 101,
                              "ciHash": "ci-1",
                              "brokerName": "증권사A",
                              "tradeType": "BUY",
                              "stockType": "FUND",
                              "qty": 5,
                              "tradeDate": "2026-03-10",
                              "amount": 500000,
                              "fundCode": "448630"
                            }
                          ]
                        }
                        """,
                                MediaType.APPLICATION_JSON));

        MydataTradeRequestDTO request =
                MydataTradeRequestDTO.builder()
                        .ciHash("ci-1")
                        .fromDate(LocalDate.of(2026, 1, 1))
                        .build();

        List<MydataTradeResponseDTO> result = mydataTradeClient.getTrades(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTradeId()).isEqualTo(100L);
        assertThat(result.get(0).getStockType()).isEqualTo("FOREIGN_STOCK");
        assertThat(result.get(0).getFundCode()).isNull();
        assertThat(result.get(1).getTradeId()).isEqualTo(101L);
        assertThat(result.get(1).getStockType()).isEqualTo("FUND");
        assertThat(result.get(1).getFundCode()).isEqualTo("448630");
        mockServer.verify();
    }

    @Test
    @DisplayName("data가 빈 배열이면 빈 리스트를 반환한다 (거래 없음은 정상 케이스)")
    void getTradesReturnsEmptyListWhenDataIsEmptyArray() {
        mockServer
                .expect(requestTo(TRADES_URL))
                .andRespond(
                        withSuccess(
                                """
                        {"message": "조회 성공", "data": []}
                        """,
                                MediaType.APPLICATION_JSON));

        List<MydataTradeResponseDTO> result =
                mydataTradeClient.getTrades(MydataTradeRequestDTO.builder().ciHash("ci-1").build());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("data가 null이면 예외를 던지지 않고 빈 리스트를 반환한다")
    void getTradesReturnsEmptyListWhenDataIsNull() {
        mockServer
                .expect(requestTo(TRADES_URL))
                .andRespond(
                        withSuccess(
                                """
                        {"message": "조회 성공", "data": null}
                        """,
                                MediaType.APPLICATION_JSON));

        List<MydataTradeResponseDTO> result =
                mydataTradeClient.getTrades(MydataTradeRequestDTO.builder().ciHash("ci-1").build());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("요청 바디에 ciHash와 fromDate가 그대로 실려 나간다")
    void getTradesSendsCiHashAndFromDateInRequestBody() {
        mockServer
                .expect(requestTo(TRADES_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"ciHash\":\"ci-1\"")))
                // RestClientConfig가 Spring 자동구성 RestClient.Builder를 주입받도록 고쳐서
                // LocalDate가 이제 ISO 문자열로 직렬화된다.
                .andExpect(content().string(containsString("\"fromDate\":\"2026-03-01\"")))
                .andRespond(
                        withSuccess(
                                """
                        {"message": "조회 성공", "data": []}
                        """,
                                MediaType.APPLICATION_JSON));

        mydataTradeClient.getTrades(
                MydataTradeRequestDTO.builder()
                        .ciHash("ci-1")
                        .fromDate(LocalDate.of(2026, 3, 1))
                        .build());

        mockServer.verify();
    }

    @Test
    @DisplayName("첫 동기화라 fromDate가 null이면 요청 바디에 fromDate가 null로 실린다")
    void getTradesSendsNullFromDateOnFirstSync() {
        mockServer
                .expect(requestTo(TRADES_URL))
                .andExpect(content().string(containsString("\"fromDate\":null")))
                .andRespond(
                        withSuccess(
                                """
                        {"message": "조회 성공", "data": []}
                        """,
                                MediaType.APPLICATION_JSON));

        mydataTradeClient.getTrades(MydataTradeRequestDTO.builder().ciHash("ci-1").build());

        mockServer.verify();
    }
}
