package com.newproject.etf.service.impl;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.repository.EtfBatchJdbcRepository;
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
import java.util.List;
import java.util.Map;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;
    private final EtfBatchJdbcRepository etfBatchJdbcRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    @Value("${api.service-key}")
    private String serviceKey;
    @Value("${api.base-url}")
    private String baseUrl;

    @Autowired
    public EtfServiceImpl(WebClient webClient, EtfBatchJdbcRepository etfBatchJdbcRepository) {
        this.webClient = webClient;
        this.etfBatchJdbcRepository = etfBatchJdbcRepository;
    }

    @Override
    public Mono<ResponseEntity<String>> list(String endPoint, EtfDto queryParams) {
        String endBasDtRaw = queryParams.getEndBasDt();
        String formattedEndBasDt = StringUtils.hasText(endBasDtRaw)
                ? LocalDate.parse(endBasDtRaw.replace("-", ""), formatter).plusDays(1).format(formatter)
                : "";

        StringBuilder url = new StringBuilder(baseUrl)
                .append(endPoint)
                .append("?resultType=json")
                .append("&serviceKey=")
                        .append(serviceKey)
                .append("&numOfRows=")
                        .append(queryParams.getNumOfRows())
                .append("&pageNo=")
                        .append(queryParams.getPageNo())
                .append("&likeItmsNm=")
                        .append(URLEncoder.encode(queryParams.getLikeItmsNm().toUpperCase(), StandardCharsets.UTF_8))
                .append("&beginBasDt=")
                        .append(queryParams.getBeginBasDt().replace("-", ""))
                .append("&endBasDt=")
                        .append(formattedEndBasDt)
                ;

        return webClient.get()
                .uri(url.toString())
                .retrieve()
                .toEntity(String.class);
    }

    @Override
    public void saveAll(List<EtfEntity> entities) {
        etfBatchJdbcRepository.saveAll(entities);
    }
}
