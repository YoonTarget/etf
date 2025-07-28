package com.newproject.etf.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 공공데이터포털 ETF API 응답의 'item' 배열 내 개별 ETF 정보를 매핑하는 DTO입니다.
 * 필드명이 JSON 키와 완전히 일치하므로 @JsonProperty는 사용하지 않습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtfDto {
    // API 응답의 필드명과 정확히 일치하므로 @JsonProperty는 불필요합니다.
    private String basDt;       // 기준일자
    private String srtnCd;      // 단축코드
    private String isinCd;      // ISIN코드
    private String itmsNm;      // 종목명
    private String clpr;        // 종가 (String으로 받음)
    private String vs;          // 대비 (String으로 받음)
    private String fltRt;       // 등락률 (String으로 받음)
    private String nav;         // 순자산가치 (String으로 받음)
    private String mkp;         // 시가 (String으로 받음)
    private String hipr;        // 고가 (String으로 받음)
    private String lopr;        // 저가 (String으로 받음)
    private String trqu;        // 거래량 (String으로 받음)
    private String trPrc;       // 거래대금 (String으로 받음)
    private String mrktTotAmt;  // 시가총액 (String으로 받음)
    private String stLstgCnt;   // 상장좌수 (String으로 받음)
    private String bssIdxIdxNm; // 기초지수_지수명
    private String bssIdxClpr;  // 기초지수_종가 (String으로 받음)
    private String nPptTotAmt;  // 순자산총액 (String으로 받음)
}