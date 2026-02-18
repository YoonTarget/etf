package com.newproject.etf.service;

import com.newproject.etf.entity.EtfInfo;
import com.newproject.etf.entity.Tag;
import com.newproject.etf.repository.EtfInfoRepository;
import com.newproject.etf.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final EtfInfoRepository etfInfoRepository;
    private final TagRepository tagRepository;

    // 태그별 키워드 매핑 (확장 가능)
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
        List<EtfInfo> allEtfs = etfInfoRepository.findAll();
        int taggedCount = 0;

        for (EtfInfo etf : allEtfs) {
            String name = etf.getItmsNm().toUpperCase(); // 대소문자 무시를 위해 대문자로 변환
            boolean isTagged = false;

            for (Map.Entry<String, List<String>> entry : TAG_KEYWORDS.entrySet()) {
                String tagName = entry.getKey();
                List<String> keywords = entry.getValue();

                for (String keyword : keywords) {
                    if (name.contains(keyword.toUpperCase())) {
                        addTagToEtf(etf, tagName);
                        isTagged = true;
                        break; // 한 태그 내에서 키워드 중 하나만 매칭되면 다음 태그로 넘어감
                    }
                }
            }
            if (isTagged) {
                taggedCount++;
            }
        }
        log.info("Auto-tagging completed. {} ETFs processed.", taggedCount);
    }

    private void addTagToEtf(EtfInfo etf, String tagName) {
        // 1. 태그가 이미 존재하는지 확인하고 없으면 생성
        Tag tag = tagRepository.findByTagName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));

        // 2. ETF에 이미 해당 태그가 붙어있는지 확인 (중복 방지)
        boolean alreadyHasTag = etf.getEtfTags().stream()
                .anyMatch(etfTag -> etfTag.getTag().getTagName().equals(tagName));

        if (!alreadyHasTag) {
            etf.addTag(tag);
            log.debug("Added tag '{}' to ETF '{}'", tagName, etf.getItmsNm());
        }
    }
}