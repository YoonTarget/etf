package com.newproject.etf.service;

import com.newproject.etf.entity.EtfInfo;
import com.newproject.etf.entity.Tag;
import com.newproject.etf.repository.EtfInfoRepository;
import com.newproject.etf.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final EtfInfoRepository etfInfoRepository;
    private final TagRepository tagRepository;

    private static final Map<String, List<String>> TAG_KEYWORDS = Map.of(
            "#반도체", List.of("반도체", "Semiconductor", "소부장"),
            "#2차전지", List.of("2차전지", "Battery", "전기차", "EV"),
            "#미국대표", List.of("S&P500", "나스닥", "NASDAQ", "Dow Jones", "다우존스", "US"),
            "#배당주", List.of("배당", "Dividend", "리츠", "REITs", "고배당", "월배당"),
            "#헬스케어", List.of("헬스케어", "Healthcare", "바이오", "Bio", "제약"),
            "#AI/로봇", List.of("AI", "인공지능", "로봇", "Robot"),
            "#채권", List.of("채권", "Bond", "국채", "회사채"),
            "#금/은", List.of("금", "Gold", "은", "Silver", "원자재"),
            "#레버리지", List.of("레버리지", "2X", "Leverage"),
            "#인버스", List.of("인버스", "Inverse", "곱버스", "-2X")
    );

    @Transactional
    public void autoTagging() {
        log.info("Starting auto-tagging process...");

        List<EtfInfo> allEtfs = etfInfoRepository.findAllWithTags();
        Map<String, Tag> tagCache = preloadTags();

        int taggedEtfCount = 0;
        int assignedTagCount = 0;

        for (EtfInfo etf : allEtfs) {
            String nameUpper = etf.getItmsNm().toUpperCase();
            Set<String> existingTagNames = new HashSet<>(
                    etf.getEtfTags().stream()
                            .map(etfTag -> etfTag.getTag().getTagName())
                            .toList()
            );

            boolean tagged = false;
            for (Map.Entry<String, List<String>> entry : TAG_KEYWORDS.entrySet()) {
                String tagName = entry.getKey();
                if (!containsAnyKeyword(nameUpper, entry.getValue())) {
                    continue;
                }

                if (existingTagNames.add(tagName)) {
                    etf.addTag(tagCache.get(tagName));
                    assignedTagCount++;
                    tagged = true;
                    log.debug("Added tag '{}' to ETF '{}'", tagName, etf.getItmsNm());
                }
            }

            if (tagged) {
                taggedEtfCount++;
            }
        }

        log.info("Auto-tagging completed. taggedEtfCount={}, assignedTagCount={}", taggedEtfCount, assignedTagCount);
    }

    private Map<String, Tag> preloadTags() {
        Map<String, Tag> tagCache = new HashMap<>();
        List<String> targetTagNames = new ArrayList<>(TAG_KEYWORDS.keySet());

        for (Tag tag : tagRepository.findByTagNameIn(targetTagNames)) {
            tagCache.put(tag.getTagName(), tag);
        }

        List<Tag> missingTags = targetTagNames.stream()
                .filter(tagName -> !tagCache.containsKey(tagName))
                .map(Tag::new)
                .toList();

        if (!missingTags.isEmpty()) {
            List<Tag> savedTags = tagRepository.saveAll(missingTags);
            for (Tag tag : savedTags) {
                tagCache.put(tag.getTagName(), tag);
            }
        }

        return tagCache;
    }

    private boolean containsAnyKeyword(String nameUpper, List<String> keywords) {
        for (String keyword : keywords) {
            if (nameUpper.contains(keyword.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}

