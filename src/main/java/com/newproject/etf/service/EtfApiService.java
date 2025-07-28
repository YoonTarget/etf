package com.newproject.etf.service;

import com.newproject.etf.dto.ApiResponse;
import com.newproject.etf.dto.EtfDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EtfApiService {

    @Value("${api.base-url}")
    private String apiUrl;
    @Value("${api.service-key}")
    private String serviceKey;

    private final WebClient webClient;

    public EtfApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    private Mono<ApiResponse> callApi(String formattedDate, int pageNo, int numOfRows) {
        System.out.println("Calling API for date: " + formattedDate + ", page: " + pageNo);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("basDt", formattedDate)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .onErrorResume(e -> {
                    System.err.println("Error calling API for date " + formattedDate + ", page " + pageNo + ": " + e.getMessage());
                    return Mono.empty(); // 오류 시 빈 Mono 반환
                });
    }

    public Flux<EtfDto> fetchAllEtfDataForDate(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int numOfRows = 10000; // API가 한 페이지에 최대로 제공하는 데이터 수

        return Mono.defer(() -> callApi(formattedDate, 1, numOfRows)) // 첫 페이지 호출
                .flatMapMany(apiResponse -> {
                    ApiResponse.Response response = apiResponse.getResponse();
                    if (response == null || response.getBody() == null) {
                        System.out.println("Invalid API response structure for date: " + formattedDate);
                        return Flux.empty();
                    }

                    ApiResponse.Body body = response.getBody();
                    int totalCount = Optional.ofNullable(body.getTotalCount()).orElse(0);

                    if (totalCount == 0) {
                        System.out.println("No data found or totalCount is 0 for date: " + formattedDate);
                        return Flux.empty();
                    }
                    System.out.println("Total items for " + formattedDate + ": " + totalCount);

                    List<EtfDto> initialItems = Optional.ofNullable(body.getItems())
                            .map(ApiResponse.Items::getItem)
                            .orElse(Collections.emptyList());

                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

                    return Flux.fromIterable(initialItems)
                            .concatWith(
                                    Flux.range(2, Math.max(0, totalPages - 1))
                                            .flatMap(pageNo -> callApi(formattedDate, pageNo, numOfRows))
                                            .flatMapIterable(res -> Optional.ofNullable(res.getResponse())
                                                    .map(ApiResponse.Response::getBody)
                                                    .map(ApiResponse.Body::getItems)
                                                    .map(ApiResponse.Items::getItem)
                                                    .orElse(Collections.emptyList())
                                            )
                            );
                })
                .doOnError(e -> System.err.println("Error in fetchAllEtfDataForDate stream: " + e.getMessage()))
                .log();
    }
}