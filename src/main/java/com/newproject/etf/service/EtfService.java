package com.newproject.etf.service;

import com.newproject.etf.entity.EtfEntity;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface EtfService {
    public Mono<ResponseEntity<String>> list(String endPoint, Map<String, String> queryParams);

    public void saveAll(List<EtfEntity> entities);
}
