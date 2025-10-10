package com.newproject.etf.dto;

// 표시 전용
public record EtfInfoView(
        String itmsNm,
        String isinCd,
        String basDt,       // yyyy.MM.dd 형식 문자열
        String nav,         // "12,345.67원"
        String mrktTotAmt   // "1.23조 원" 또는 "123억 원"
) {}

