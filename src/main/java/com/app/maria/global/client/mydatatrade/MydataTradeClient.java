package com.app.maria.global.client.mydatatrade;

import com.app.maria.domain.externaltradesync.dto.request.MydataTradeRequestDTO;
import com.app.maria.domain.externaltradesync.dto.response.MydataTradeResponseDTO;
import com.app.maria.global.response.ApiResponseDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MydataTradeClient {

    private final RestClient restClient;

    public MydataTradeClient(@Qualifier("mydataRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MydataTradeResponseDTO> getTrades(MydataTradeRequestDTO request) {
        ApiResponseDTO<List<MydataTradeResponseDTO>> apiResponse =
                restClient
                        .post()
                        .uri("/api/mydata/trades")
                        .body(request)
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference<
                                        ApiResponseDTO<List<MydataTradeResponseDTO>>>() {});
        if (apiResponse == null || apiResponse.getData() == null) {
            return List.of();
        }

        return apiResponse.getData();
    }
}
