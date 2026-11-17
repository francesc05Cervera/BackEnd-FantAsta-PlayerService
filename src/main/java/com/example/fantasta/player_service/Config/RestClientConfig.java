package com.example.fantasta.player_service.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import com.example.fantasta.player_service.Integration.ApiFootballProperties;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient authRestClient(@Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(2000);
        requests.setReadTimeout(5000);
        return RestClient.builder()
                .requestFactory(requests)
                .baseUrl(authServiceUrl)
                .build();
    }

    @Bean
    public RestClient apiFootballRestClient(ApiFootballProperties properties) {
        SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(properties.getConnectTimeoutMs());
        requests.setReadTimeout(properties.getReadTimeoutMs());
        return RestClient.builder().requestFactory(requests).baseUrl(properties.getBaseUrl()).build();
    }
}
