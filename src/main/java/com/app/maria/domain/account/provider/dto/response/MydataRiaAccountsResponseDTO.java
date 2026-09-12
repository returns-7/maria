package com.app.maria.domain.account.provider.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MydataRiaAccountsResponseDTO {
    private String message;
    private List<MyDataAccountResponse> data;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MyDataAccountResponse {
        private Long mydataAccountId;
        private String brokerName;
        private BigDecimal riaLimit;
        private BigDecimal riaCumulativeSell;
    }
}
