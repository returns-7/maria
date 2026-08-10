package com.app.maria.domain.account.provider;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.dto.response.MydataRiaAccountsResponseDTO;
import com.app.maria.global.exception.MydataApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class MydataProviderImpl implements MydataProvider {
  private static final String RIA_ACCOUNTS_PATH = "/api/mydata/ria-accounts";
  private static final String CREATE_RIA_ACCOUNT_PATH = RIA_ACCOUNTS_PATH + "/save";
  private static final String UPDATE_RIA_LIMIT_PATH = RIA_ACCOUNTS_PATH + "/limit-update";

  private final RestClient restClient;

  @Value("${custom.mydata.url}")
  private String myDataUrl;
  @Value("${custom.mydata.own-broker-name}")
  private String ownBrokerName;

  @Override
  public BigDecimal getExternalConfiguredLimit(String ciHash) {
    MydataRiaAccountsResponseDTO response = getRiaAccounts(ciHash);
    if (response == null || response.getData() == null) {
      throw new MydataApiException("myData 계좌 한도 조회 응답이 올바르지 않습니다.", null);
    }

    BigDecimal configuredLimit = BigDecimal.ZERO;
    for (MydataRiaAccountsResponseDTO.MyDataAccountResponse account : response.getData()) {
      if (account == null || account.getRiaLimit() == null || account.getRiaLimit().signum() < 0) {
        throw new MydataApiException("myData 계좌 한도 응답이 올바르지 않습니다.", null);
      }
      if (!ownBrokerName.equals(account.getBrokerName())) {
        configuredLimit = configuredLimit.add(account.getRiaLimit());
      }
    }
    return configuredLimit;
  }

  @Override
  public MydataRiaAccountsResponseDTO getRiaAccounts(String ciHash) {
    log.info("ciHash: {}", ciHash);
    Map<String, String> req = new HashMap<>();
    req.put("ciHash", ciHash);
    try {
      return restClient.post().uri(myDataUrl + RIA_ACCOUNTS_PATH)
          .contentType(MediaType.APPLICATION_JSON).body(req).retrieve()
          .body(MydataRiaAccountsResponseDTO.class);
    } catch (RestClientException exception) {
      throw new MydataApiException("myData 계좌 한도 조회 실패", exception);
    }
  }

  @Override
  public HttpStatusCode createRiaAccount(String ciHash, AccountDTO account) {
    log.info("ciHash: {}", ciHash);
    Map<String, String> req = new HashMap<>();
    req.put("ciHash", ciHash);
    req.put("brokerName", ownBrokerName);
    req.put("riaLimit", String.valueOf(account.getLimitAmount()));
    req.put("riaCumulativeSell", String.valueOf(0));

    try {
      ResponseEntity<?> response = restClient.post().uri(myDataUrl + CREATE_RIA_ACCOUNT_PATH)
          .contentType(MediaType.APPLICATION_JSON).body(req).retrieve()
          .toEntity(Object.class);
      return response.getStatusCode();
    } catch (RestClientException exception) {
      throw new MydataApiException("myData 계좌 등록 실패", exception);
    }
  }

  @Override
  public HttpStatusCode updateRiaLimit(String ciHash, AccountDTO account) {
    log.info("ciHash: {}", ciHash);
    Map<String, String> req = new HashMap<>();
    req.put("ciHash", ciHash);
    req.put("brokerName", ownBrokerName);
    req.put("riaLimit", String.valueOf(account.getLimitAmount()));

    try {
      ResponseEntity<?> response = restClient.put().uri(myDataUrl + UPDATE_RIA_LIMIT_PATH)
          .contentType(MediaType.APPLICATION_JSON).body(req).retrieve()
          .toEntity(Object.class);
      return response.getStatusCode();
    } catch (RestClientException exception) {
      throw new MydataApiException("myData 계좌 한도 변경 실패", exception);
    }
  }

}
