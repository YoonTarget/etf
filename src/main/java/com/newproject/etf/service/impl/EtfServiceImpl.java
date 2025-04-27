package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    @Value("${api.service-key}")
    private String serviceKey;

    @Autowired
    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ResponseEntity<String>> list(String endPoint, Map<String, String> queryParams) {
        String endBasDtRaw = queryParams.get("endBasDt");
        String formattedEndBasDt = StringUtils.hasText(endBasDtRaw)
                ? LocalDate.parse(endBasDtRaw.replace("-", ""), formatter).plusDays(1).format(formatter)
                : "";

        StringBuilder url = new StringBuilder("https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService/")
                .append(endPoint)
                .append("?resultType=json")
                .append("&serviceKey=")
                        .append(serviceKey)
                .append("&numOfRows=")
                        .append(queryParams.getOrDefault("numOfRows", "10"))
                .append("&pageNo=")
                        .append(queryParams.getOrDefault("pageNo", "1"))
                .append("&likeItmsNm=")
                        .append(URLEncoder.encode(queryParams.getOrDefault("likeItmsNm", "").toUpperCase(), StandardCharsets.UTF_8))
                .append("&beginBasDt=")
                        .append(queryParams.getOrDefault("beginBasDt", "").replace("-", ""))
                .append("&endBasDt=")
                        .append(formattedEndBasDt)
                ;

        return webClient.get()
                .uri(url.toString())
                .retrieve()
                .toEntity(String.class);
    }
}
