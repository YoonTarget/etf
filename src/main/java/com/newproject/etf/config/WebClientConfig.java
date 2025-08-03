package com.newproject.etf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {
    private final DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
    private static final int MAX_BUFFER_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

    @Bean
    public WebClient webClient() {
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return WebClient.builder()
                .uriBuilderFactory(factory)
                // Add the codecs configuration here to increase the buffer size
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE_BYTES))
                .build();
    }
}
