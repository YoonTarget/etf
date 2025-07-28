package com.newproject.etf.service.impl;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.service.EtfService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EtfServiceImpl implements EtfService {
    @Override
    public Mono<ResponseEntity<String>> list(String endPoint, EtfDto queryParams) {
        return null;
    }
}
