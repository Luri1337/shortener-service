package com.dima.shortener_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClient {
    @Value("${analytics.service.url}")
    private String analyticsServiceUrl;

    @Bean
    public RestClient analyticsWebClient(){
        return RestClient.builder()
                .baseUrl(analyticsServiceUrl)
                .build();
    }
}
