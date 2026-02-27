package com.newproject.etf.service;

import com.newproject.etf.dto.EtfSummaryDto;
import com.newproject.etf.dto.TagDto;
import com.newproject.etf.repository.EtfInfoRepository;
import com.newproject.etf.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EtfSearchService {

    private final EtfInfoRepository etfInfoRepository;
    private final EtfRepository etfRepository;

    @Cacheable(value = "allTags")
    public List<TagDto> findAllTags() {
        return etfInfoRepository.findAllTagsWithCount();
    }

    public List<EtfSummaryDto> findEtfsByTag(String tagName) {
        return findEtfsByTag(tagName, 120);
    }

    @Cacheable(value = "etfsByTag", key = "#tagName + ':' + #limit")
    public List<EtfSummaryDto> findEtfsByTag(String tagName, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return etfRepository.findEtfSummariesByTag(tagName, safeLimit).stream()
                .map(row -> convertToSummaryDto(row, tagName))
                .toList();
    }

    @Cacheable(value = "etfsBySearch", key = "#query + ':' + #limit")
    public List<EtfSummaryDto> searchEtfs(String query, int limit) {
        String safeQuery = query == null ? "" : query.trim();
        if (safeQuery.isEmpty()) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return etfRepository.findEtfSummariesByKeyword(safeQuery, safeLimit).stream()
                .map(row -> convertToSummaryDto(row, null))
                .toList();
    }

    private EtfSummaryDto convertToSummaryDto(EtfRepository.EtfSummaryRow row, String requestedTagName) {
        return EtfSummaryDto.builder()
                .srtnCd(row.getSrtnCd())
                .itmsNm(row.getItmsNm())
                .clpr(row.getClpr())
                .fltRt(row.getFltRt())
                .vs(row.getVs())
                .trqu(row.getTrqu())
                .mrktTotAmt(row.getMrktTotAmt())
                .tags(requestedTagName == null ? List.of() : List.of(requestedTagName))
                .build();
    }
}

