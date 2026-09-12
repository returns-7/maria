package com.app.maria.domain.account.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AccountMydataRestClientConfig {

    @Bean
    public RestClient accountMydataRestClient(
            RestClient.Builder builder, @Value("${custom.mydata.url}") String mydataUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return builder.clone().baseUrl(mydataUrl).requestFactory(requestFactory).build();
    }
}
