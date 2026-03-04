package com.newproject.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtfSummaryDto {
    private String srtnCd;      // 종목코드
    private String itmsNm;      // 종목명
    private String basDt;       // 기준일자 (YYYYMMDD)
    private BigDecimal clpr;    // 현재가 (종가)
    private BigDecimal fltRt;   // 등락률
    private BigDecimal vs;      // 대비 (전일비)
    private Long trqu;          // 거래량
    private Long mrktTotAmt;    // 시가총액
    private List<String> tags;  // 연관 태그 목록
}
