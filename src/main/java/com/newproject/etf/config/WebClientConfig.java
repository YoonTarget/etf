package com.newproject.etf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {

    @Value("${spring.codec.max-in-memory-size}")
    private int maxBufferSize;

    private final DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();

    @Bean
    public WebClient webClient() {
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBufferSize))
                .build();

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
