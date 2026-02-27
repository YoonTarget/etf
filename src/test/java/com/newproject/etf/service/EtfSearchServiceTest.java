package com.newproject.etf.service;

import com.newproject.etf.dto.EtfSummaryDto;
import com.newproject.etf.dto.TagDto;
import com.newproject.etf.repository.EtfInfoRepository;
import com.newproject.etf.repository.EtfRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EtfSearchServiceTest {

    @Mock
    private EtfInfoRepository etfInfoRepository;

    @Mock
    private EtfRepository etfRepository;

    @InjectMocks
    private EtfSearchService etfSearchService;

    @Test
    @DisplayName("find all tags")
    void findAllTags() {
        TagDto tag1 = new TagDto(1L, "#반도체", 10);
        TagDto tag2 = new TagDto(2L, "#배당주", 5);
        given(etfInfoRepository.findAllTagsWithCount()).willReturn(List.of(tag1, tag2));

        List<TagDto> result = etfSearchService.findAllTags();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTagName()).isEqualTo("#반도체");
        assertThat(result.get(0).getEtfCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("find ETFs by tag")
    void findEtfsByTag() {
        String tagName = "#반도체";
        EtfRepository.EtfSummaryRow row = new EtfRepository.EtfSummaryRow() {
            @Override
            public String getSrtnCd() { return "091160"; }
            @Override
            public String getItmsNm() { return "KODEX 반도체"; }
            @Override
            public BigDecimal getClpr() { return new BigDecimal("35000"); }
            @Override
            public BigDecimal getFltRt() { return new BigDecimal("1.5"); }
            @Override
            public BigDecimal getVs() { return new BigDecimal("520"); }
            @Override
            public Long getTrqu() { return 1800000L; }
            @Override
            public Long getMrktTotAmt() { return 1300000000000L; }
        };

        given(etfRepository.findEtfSummariesByTag(tagName, 120)).willReturn(List.of(row));

        List<EtfSummaryDto> result = etfSearchService.findEtfsByTag(tagName);

        assertThat(result).hasSize(1);
        EtfSummaryDto dto = result.get(0);
        assertThat(dto.getSrtnCd()).isEqualTo("091160");
        assertThat(dto.getItmsNm()).isEqualTo("KODEX 반도체");
        assertThat(dto.getClpr()).isEqualTo(new BigDecimal("35000"));
        assertThat(dto.getTags()).contains("#반도체");
    }

    @Test
    @DisplayName("search ETFs by keyword")
    void searchEtfs() {
        EtfRepository.EtfSummaryRow row = new EtfRepository.EtfSummaryRow() {
            @Override
            public String getSrtnCd() { return "379810"; }
            @Override
            public String getItmsNm() { return "KODEX 미국나스닥100"; }
            @Override
            public BigDecimal getClpr() { return new BigDecimal("22940"); }
            @Override
            public BigDecimal getFltRt() { return new BigDecimal("-0.26"); }
            @Override
            public BigDecimal getVs() { return new BigDecimal("-60"); }
            @Override
            public Long getTrqu() { return 1288200L; }
            @Override
            public Long getMrktTotAmt() { return 3350387000000L; }
        };

        given(etfRepository.findEtfSummariesByKeyword("나스닥", 120)).willReturn(List.of(row));

        List<EtfSummaryDto> result = etfSearchService.searchEtfs("나스닥", 120);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSrtnCd()).isEqualTo("379810");
        assertThat(result.get(0).getItmsNm()).isEqualTo("KODEX 미국나스닥100");
        assertThat(result.get(0).getClpr()).isEqualTo(new BigDecimal("22940"));
    }
}
