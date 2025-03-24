package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;

    @Autowired
    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ResponseEntity<String>> list(String endPoint, Map<String, String> queryParams) {
        String url = "https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService/" + endPoint
                + "?serviceKey=BBDYHxpLb5iDQfFrXs95dcZqTnYTBG%2B%2Bo6bPr0BC9bmIHnG5gB48wToN04d4DM8uRSj7m5ha1mQvRdLJ%2Fpss9Q%3D%3D"  // 직접 추가 (자동 인코딩 방지)
                + "&numOfRows=" + queryParams.getOrDefault("numOfRows", "10")
                + "&pageNo=" + queryParams.getOrDefault("pageNo", "1")
                + "&resultType=json"; // JSON 응답 강제 요청

        System.out.println("🚀 API 요청 URL: " + url); // 요청 URL 로그 출력

        return webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);
    }
}
