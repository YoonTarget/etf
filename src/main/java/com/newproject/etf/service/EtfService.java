package com.newproject.etf.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface EtfService {
    Mono<ResponseEntity<String>> list(String endPoint, Map<String, String> queryParams);
}
