package com.newproject.etf.controller;

import com.newproject.etf.dto.EtfSummaryDto;
import com.newproject.etf.dto.TagDto;
import com.newproject.etf.service.EtfSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EtfSearchController {

    private final EtfSearchService etfSearchService;

    // 모든 태그 목록 조회 (메뉴판)
    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(etfSearchService.findAllTags());
    }

    // 특정 태그에 속한 ETF 목록 조회 (주문)
    @GetMapping("/etfs/by-tag/{tagName}")
    public ResponseEntity<List<EtfSummaryDto>> getEtfsByTag(@PathVariable String tagName,
                                                            @RequestParam(defaultValue = "120") int limit) {
        // URL 인코딩된 태그 이름 (#반도체 -> %23반도체) 처리
        // 보통 프론트엔드에서 #을 빼고 보내거나 인코딩해서 보냄
        // 여기서는 #이 없으면 붙여주는 로직 추가 (편의성)
        if (!tagName.startsWith("#")) {
            tagName = "#" + tagName;
        }
        return ResponseEntity.ok(etfSearchService.findEtfsByTag(tagName, limit));
    }

    @GetMapping("/etfs/search")
    public ResponseEntity<List<EtfSummaryDto>> searchEtfs(@RequestParam String query,
                                                          @RequestParam(defaultValue = "120") int limit) {
        return ResponseEntity.ok(etfSearchService.searchEtfs(query, limit));
    }
}
