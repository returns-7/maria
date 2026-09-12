package com.app.maria.domain.account.provider;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.provider.dto.response.MydataRiaAccountsResponseDTO;
import com.app.maria.global.exception.MydataApiException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MydataProviderImpl implements MydataProvider {
    private static final String RIA_ACCOUNTS_PATH = "/api/mydata/ria-accounts";
    private static final String SYNC_RIA_ACCOUNT_PATH = RIA_ACCOUNTS_PATH + "/save";
    private final RestClient restClient;

    public MydataProviderImpl(@Qualifier("accountMydataRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

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
            if (account == null
                    || account.getRiaLimit() == null
                    || account.getRiaLimit().signum() < 0) {
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
        Map<String, String> req = new HashMap<>();
        req.put("ciHash", ciHash);
        try {
            return restClient
                    .post()
                    .uri(myDataUrl + RIA_ACCOUNTS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(MydataRiaAccountsResponseDTO.class);
        } catch (RestClientResponseException exception) {
            // 외부 오류 본문에는 ciHash 등 요청 정보가 포함될 수 있어 원본 예외를 전달하지 않는다.
            throw new MydataApiException(
                    "myData 계좌 한도 조회 실패 (HTTP " + exception.getStatusCode().value() + ")", null);
        } catch (RestClientException exception) {
            throw new MydataApiException("myData 계좌 한도 조회 실패", exception);
        }
    }

    @Override
    public HttpStatusCode syncRiaAccount(String ciHash, AccountDTO account) {
        Map<String, String> req = new HashMap<>();
        req.put("ciHash", ciHash);
        req.put("brokerName", ownBrokerName);
        req.put("riaLimit", String.valueOf(account.getLimitAmount()));

        try {
            ResponseEntity<?> response =
                    restClient
                            .post()
                            .uri(myDataUrl + SYNC_RIA_ACCOUNT_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(req)
                            .retrieve()
                            .toEntity(Object.class);
            return response.getStatusCode();
        } catch (RestClientResponseException exception) {
            throw new MydataApiException(
                    "myData RIA 계좌 동기화 실패 (HTTP " + exception.getStatusCode().value() + ")", null);
        } catch (RestClientException exception) {
            throw new MydataApiException("myData RIA 계좌 동기화 실패", exception);
        }
    }
}
