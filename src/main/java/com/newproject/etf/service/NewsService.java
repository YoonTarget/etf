package com.newproject.etf.service;

import com.newproject.etf.dto.NewsItemDto;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {
    public List<NewsItemDto> getNewsByQuery(String query) throws UnsupportedEncodingException {
        // ✅ 1. 검색어(query)를 UTF-8로 URL 인코딩
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // 2. 인코딩된 쿼리를 URL에 사용
        String rssUrl = "https://news.google.com/rss/search?q=" + encodedQuery + "&hl=ko&gl=KR&ceid=KR:ko";

        try {
            // 1. URL 객체 생성
            URL feedUrl = new URL(rssUrl);

            // 2. XmlReader를 통해 URL에서 직접 데이터를 읽음 (HTTP 요청 발생)
            //    XmlReader는 XML 문서의 인코딩을 자동으로 감지합니다.
            try (XmlReader reader = new XmlReader(feedUrl)) {
                // 3. SyndFeedInput을 사용하여 XML 스트림을 ROME 객체(SyndFeed)로 파싱
                SyndFeed feed = new SyndFeedInput().build(reader);

                // 4. 피드 항목(Entry)을 순회하며 필요한 정보를 DTO로 변환하여 반환
                return feed.getEntries().stream()
                        .map(entry -> new NewsItemDto(
                                entry.getTitle(),
                                entry.getLink(),
                                entry.getUri(), // ✅ guid 대신 getUri() 사용
                                entry.getPublishedDate(),
                                // ✅ description은 객체를 반환하므로 .getValue()를 호출해야 함
                                entry.getDescription() != null ? entry.getDescription().getValue() : "",
                                // ✅ source는 SyndEntrySource 객체를 반환하므로 .getTitle()로 이름 추출
                                entry.getSource() != null ? entry.getSource().getTitle() : "미상"
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // 네트워크 오류, 파싱 오류 등 예외 처리
            System.err.println("RSS 피드 처리 중 오류 발생: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
