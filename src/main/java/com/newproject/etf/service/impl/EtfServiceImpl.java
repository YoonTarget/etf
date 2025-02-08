package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;

    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String list(String endPoint) {
        return webClient.get()
                .uri("https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
