package com.newproject.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    private Long id;
    private String tagName;
    private long etfCount; // 해당 태그에 속한 ETF 개수
}