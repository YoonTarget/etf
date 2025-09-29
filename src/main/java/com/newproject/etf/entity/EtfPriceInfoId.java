package com.newproject.etf.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EtfPriceInfoId implements Serializable {
    private String basDt; // 기준일자
    private String srtnCd; // 단축코드
}
