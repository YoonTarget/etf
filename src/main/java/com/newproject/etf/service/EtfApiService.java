package com.newproject.etf.service;

import com.newproject.etf.dto.ApiResponse;
import com.newproject.etf.dto.EtfDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
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
        System.out.println("Calling external API with pageNo: " + pageNo + ", numOfRows: " + numOfRows);

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
                .bodyToMono(ApiResponse.class) // Expect a wrapper object
                .flatMapMany(response -> {
                    // Check if the response or items are null to avoid NPE
                    if (response != null && response.getResponse() != null &&
                            response.getResponse().getBody() != null &&
                            response.getResponse().getBody().getItems() != null &&
                            response.getResponse().getBody().getItems().getItem() != null) {
                        return Flux.fromIterable(response.getResponse().getBody().getItems().getItem());
                    }
                    System.out.println("API returned no data or invalid structure for page " + pageNo);
                    return Flux.empty(); // Return empty Flux if no data or invalid structure
                });
    }
}