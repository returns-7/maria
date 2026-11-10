package com.app.maria.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "custom.price")
public class PriceApiProperties {

    private String name;
    private String url;
    private String appKey;
    private String appSecret;
}
