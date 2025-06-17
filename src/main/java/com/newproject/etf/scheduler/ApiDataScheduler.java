package com.newproject.etf.scheduler;

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
        Map<String, String> queryParams = Map.of(
                "numOfRows", "1000",
                "pageNo", "1",
                "beginBasDt", "20230101",
                "endBasDt", "20231231"
        );
        etfService.list(endPoint, queryParams);
    }
}