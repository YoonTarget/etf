package com.newproject.etf.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 공공데이터포털 ETF API의 최상위 응답 구조를 매핑하는 DTO입니다.
 * 중첩된 JSON 구조를 그대로 반영합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private Response response; // 최상위 "response" 필드

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Header header; // "header" 필드
        private Body body;     // "body" 필드
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String resultCode; // "resultCode"
        private String resultMsg;  // "resultMsg"
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private Integer numOfRows;  // "numOfRows"
        private Integer pageNo;     // "pageNo"
        private Integer totalCount; // "totalCount"
        private Items items;        // "items" 필드
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {
        // "item"은 JSON 배열이므로 List<EtfDto>로 매핑합니다.
        // 여기서 EtfDto는 개별 ETF 정보를 담는 DTO입니다.
        private List<EtfDto> item;
    }
}
