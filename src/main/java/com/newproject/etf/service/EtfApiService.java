package com.newproject.etf.service;

import com.newproject.etf.dto.ApiResponse;
import com.newproject.etf.dto.EtfDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class EtfApiService {

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.service-key}")
    private String serviceKey;

    private final WebClient webClient;

    public EtfApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<EtfDto> fetchEtfData(int pageNo, int numOfRows) {
        log.info("Calling external API with pageNo: {}, numOfRows: {}", pageNo, numOfRows);

        String url = baseUrl +
                "?serviceKey=" +
                serviceKey +
                "&numOfRows=" +
                numOfRows +
                "&pageNo=" +
                pageNo +
                "&resultType=json"
        ;

        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                // [Resilience] 네트워크 오류 등 일시적 장애 시 2초 간격으로 최대 3회 재시도
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof Exception)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("API call failed after retries for page {}: {}", pageNo, retrySignal.failure().getMessage());
                            return new RuntimeException("API call failed after retries", retrySignal.failure());
                        }))
                .flatMapMany(response -> {
                    if (response != null && response.getResponse() != null &&
                            response.getResponse().getBody() != null &&
                            response.getResponse().getBody().getItems() != null &&
                            response.getResponse().getBody().getItems().getItem() != null) {
                        return Flux.fromIterable(response.getResponse().getBody().getItems().getItem());
                    }
                    log.warn("API returned no data or invalid structure for page {}", pageNo);
                    return Flux.empty();
                });
    }
}