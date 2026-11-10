package com.app.maria.global.client.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KisTokenRequest {

    @JsonProperty("grant_type")
    private String grantType;

    private String appkey;
    private String appsecret;
}
