package com.newproject.etf.dto;

// 표시 전용
public record EtfInfoView(
        String itmsNm, // 종목명
        String isinCd, // ISIN코드
        String bssIdxIdxNm, // 기초지수_지수명
        String bssIdxClpr, // 기초지수_종가
        String mrktTotAmt, // 시가총액("1.23조 원" 또는 "123억 원")
        String nav, // 순자산가치("12,345.67원")
        String basDt // 기준일자(yyyy.MM.dd 형식 문자열)
) {}

