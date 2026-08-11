package com.app.maria.domain.account.provider;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.type.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
    mockServer.expect(requestTo(MYDATA_URL + "/api/mydata/ria-accounts/save"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"ciHash\":\"ci-hash\"")))
        .andExpect(content().string(containsString("\"brokerName\":\"리턴증권\"")))
        .andExpect(content().string(containsString("\"riaLimit\":\"40000000\"")))
        .andExpect(content().string(not(containsString("riaCumulativeSell"))))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    assertThat(provider.syncRiaAccount("ci-hash", openedAccount()).value()).isEqualTo(HttpStatus.OK.value());

    mockServer.verify();
  }

  private AccountDTO openedAccount() {
    return AccountDTO.builder()
        .accountId(1L)
        .status(Status.OPENED)
        .limitAmount(new BigDecimal("40000000"))
        .build();
  }
}
