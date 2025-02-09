package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;
    private StringBuilder sb;

    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String list(String url, String serviceKey, String endPoint) {
        sb = new StringBuilder(url);
        sb.append(endPoint).append("?serviceKey=").append(serviceKey);
        return webClient.get()
                .uri(sb.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
