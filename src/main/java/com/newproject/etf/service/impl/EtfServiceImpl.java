package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<ResponseEntity<String>> list(String endPoint, Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder("https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService/")
                .append(endPoint)
                .append("?serviceKey=BBDYHxpLb5iDQfFrXs95dcZqTnYTBG%2B%2Bo6bPr0BC9bmIHnG5gB48wToN04d4DM8uRSj7m5ha1mQvRdLJ%2Fpss9Q%3D%3D")
                .append("&numOfRows=").append(queryParams.getOrDefault("numOfRows", "10"))
                .append("&pageNo=").append(queryParams.getOrDefault("pageNo", "1"))
//                .append("&basDt=").append(queryParams.getOrDefault("basDt", ""))
                .append("&likeItmsNm=").append(URLEncoder.encode(queryParams.getOrDefault("likeItmsNm", "").toUpperCase(), StandardCharsets.UTF_8))
                .append("&beginBasDt=").append(queryParams.getOrDefault("beginBasDt", "").replace("-", ""))
                .append("&endBasDt=").append(StringUtils.hasText(queryParams.get("endBasDt")) ? LocalDate.parse(queryParams.get("endBasDt").replace("-", ""), formatter).plusDays(1).format(formatter) : "")
                .append("&resultType=json");

        return webClient.get()
                .uri(url.toString())
                .retrieve()
                .toEntity(String.class);
    }
}
