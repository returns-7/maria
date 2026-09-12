package com.app.maria.domain.account.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.provider.dto.response.MydataRiaAccountsResponseDTO;
import com.app.maria.domain.account.type.Status;
import com.app.maria.global.exception.MydataApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MydataProviderImplTest {
    private static final String MYDATA_URL = "http://mydata.test";

    private MockRestServiceServer mockServer;
    private MydataProviderImpl provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(MYDATA_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        provider = new MydataProviderImpl(builder.build());
        ReflectionTestUtils.setField(provider, "myDataUrl", MYDATA_URL);
        ReflectionTestUtils.setField(provider, "ownBrokerName", "리턴증권");
    }

    @Test
    void syncRiaAccountSendsLatestLimitToMydataSave() {
        mockServer
                .expect(requestTo(MYDATA_URL + "/api/mydata/ria-accounts/save"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"ciHash\":\"ci-hash\"")))
                .andExpect(content().string(containsString("\"brokerName\":\"리턴증권\"")))
                .andExpect(content().string(containsString("\"riaLimit\":\"40000000\"")))
                .andExpect(content().string(not(containsString("riaCumulativeSell"))))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(provider.syncRiaAccount("ci-hash", openedAccount()).value())
                .isEqualTo(HttpStatus.OK.value());

        mockServer.verify();
    }

    private AccountDTO openedAccount() {
        return AccountDTO.builder()
                .accountId(1L)
                .status(Status.OPENED)
                .limitAmount(new BigDecimal("40000000"))
                .build();
    }

    @Test
    void externalLimitUsesFreshResponseAndExcludesOwnBroker() {
        for (int limit : new int[] {1000, 2000}) {
            mockServer
                    .expect(requestTo(MYDATA_URL + "/api/mydata/ria-accounts"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().json("{\"ciHash\":\"test-ci\"}"))
                    .andRespond(
                            withSuccess(
                                    """
                            {"data":[
                              {"ciHash":"test-ci","brokerName":"리턴증권","riaLimit":3000},
                              {"ciHash":"test-ci","brokerName":"외부증권","riaLimit":%d}
                            ]}
                            """
                                            .formatted(limit),
                                    MediaType.APPLICATION_JSON));
        }
        assertThat(provider.getExternalConfiguredLimit("test-ci")).isEqualByComparingTo("1000");
        assertThat(provider.getExternalConfiguredLimit("test-ci")).isEqualByComparingTo("2000");
        mockServer.verify();
    }

    @Test
    void externalResponseDoesNotRetainCiHash() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MydataRiaAccountsResponseDTO response =
                mapper.readValue(
                        """
                {"data":[{"ciHash":"sensitive-ci","brokerName":"외부증권","riaLimit":1000}]}
                """,
                        MydataRiaAccountsResponseDTO.class);
        assertThat(response.getData().get(0).getRiaLimit()).isEqualByComparingTo("1000");
        assertThat(mapper.writeValueAsString(response)).doesNotContain("ciHash", "sensitive-ci");
        assertThat(response.toString()).doesNotContain("sensitive-ci");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void httpErrorDoesNotExposeUpstreamBodyInExceptionLogs(boolean sync) {
        String path = "/api/mydata/ria-accounts" + (sync ? "/save" : "");
        mockServer
                .expect(requestTo(MYDATA_URL + path))
                .andRespond(
                        withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        "{\"ciHash\":\"sensitive-ci\",\"message\":\"upstream detail\"}"));
        assertThatThrownBy(
                        () -> {
                            if (sync) provider.syncRiaAccount("sensitive-ci", openedAccount());
                            else provider.getExternalConfiguredLimit("sensitive-ci");
                        })
                .isInstanceOf(MydataApiException.class)
                .hasMessageContaining("HTTP 500")
                .satisfies(
                        error -> {
                            StringWriter stackTrace = new StringWriter();
                            error.printStackTrace(new PrintWriter(stackTrace));
                            assertThat(stackTrace.toString())
                                    .doesNotContain("sensitive-ci", "upstream detail");
                        });
        mockServer.verify();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"data\":null}",
                "{\"data\":[null]}",
                "{\"data\":[{\"brokerName\":\"외부증권\",\"riaLimit\":-1}]}"
            })
    void invalidExternalLimitResponseFailsClosed(String body) {
        mockServer
                .expect(requestTo(MYDATA_URL + "/api/mydata/ria-accounts"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> provider.getExternalConfiguredLimit("test-ci"))
                .isInstanceOf(MydataApiException.class);
        mockServer.verify();
    }
}
