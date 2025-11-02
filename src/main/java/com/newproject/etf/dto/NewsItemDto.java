package com.newproject.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 뉴스 아이템 정보를 매핑하는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsItemDto {
    private String title;       // 뉴스 제목
    private String link;        // 뉴스 링크
    private String guid;        // 고유 식별자
    private Date pubDate;     // 발행일자
    private String description; // 뉴스 요약
    private String source;      // 뉴스 출처
}
