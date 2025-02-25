package com.newproject.etf.service.impl;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EtfServiceImpl implements EtfService {

    private final WebClient webClient;
    private final String url = "https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService";
    private final String serviceKey = "BBDYHxpLb5iDQfFrXs95dcZqTnYTBG%2B%2Bo6bPr0BC9bmIHnG5gB48wToN04d4DM8uRSj7m5ha1mQvRdLJ%2Fpss9Q%3D%3D";
    private final String numOfRowsInit = "1";
    private final String pageNoInit = "1";
    private final String resultTypeInit = "json";
    private StringBuilder sb;

    @Autowired
    public EtfServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String list(String endPoint, Map<String, String> queryParams) {
        String numOfRows = queryParams.getOrDefault("numOfRows", numOfRowsInit);
        String pageNo = queryParams.getOrDefault("pageNo", pageNoInit);
        String resultType = queryParams.getOrDefault("resultType", resultTypeInit);

        sb = new StringBuilder(url);
        sb.append("/").append(endPoint)
                .append("?serviceKey=").append(serviceKey)
                .append("&numOfRows=").append(numOfRows)
                .append("&pageNo=").append(pageNo)
                .append("&resultType=").append(resultType);
        return webClient.get()
                .uri(sb.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
