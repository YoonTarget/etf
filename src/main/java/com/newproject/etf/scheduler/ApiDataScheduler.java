package com.newproject.etf.scheduler;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.service.EtfService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApiDataScheduler {

    private final EtfService etfService;

    public ApiDataScheduler(EtfService etfService) {
        this.etfService = etfService;
    }

//    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void updateEtfData() {
        String endPoint = "getETFPriceInfo";
        EtfDto queryParams = null;

        etfService.list(endPoint, queryParams);
    }
}