package com.newproject.etf.controller;

import com.newproject.etf.dto.EtfSummaryDto;
import com.newproject.etf.dto.TagDto;
import com.newproject.etf.service.EtfSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EtfSearchController.class)
class EtfSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EtfSearchService etfSearchService;

    @Test
    @DisplayName("모든 태그 목록을 조회한다")
    void getAllTags() throws Exception {
        // given
        TagDto tag1 = new TagDto(1L, "#반도체", 10);
        TagDto tag2 = new TagDto(2L, "#배당주", 5);
        given(etfSearchService.findAllTags()).willReturn(List.of(tag1, tag2));

        // when & then
        mockMvc.perform(get("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagName").value("#반도체"))
                .andExpect(jsonPath("$[0].etfCount").value(10))
                .andExpect(jsonPath("$[1].tagName").value("#배당주"))
                .andExpect(jsonPath("$[1].etfCount").value(5));
    }

    @Test
    @DisplayName("특정 태그에 속한 ETF 목록을 조회한다")
    void getEtfsByTag() throws Exception {
        // given
        String tagName = "#반도체";
        EtfSummaryDto dto = EtfSummaryDto.builder()
                .srtnCd("091160")
                .itmsNm("KODEX 반도체")
                .clpr(new BigDecimal("35000"))
                .tags(List.of("#반도체"))
                .build();
        given(etfSearchService.findEtfsByTag(tagName, 120)).willReturn(List.of(dto));

        // when & then
        mockMvc.perform(get("/api/v1/etfs/by-tag/{tagName}", tagName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].srtnCd").value("091160"))
                .andExpect(jsonPath("$[0].itmsNm").value("KODEX 반도체"))
                .andExpect(jsonPath("$[0].clpr").value(35000));
    }

    @Test
    @DisplayName("검색어로 ETF 목록을 조회한다")
    void searchEtfs() throws Exception {
        EtfSummaryDto dto = EtfSummaryDto.builder()
                .srtnCd("379810")
                .itmsNm("KODEX 미국나스닥100")
                .clpr(new BigDecimal("22940"))
                .build();
        given(etfSearchService.searchEtfs("나스닥", 120)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/etfs/search")
                        .param("query", "나스닥")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].srtnCd").value("379810"))
                .andExpect(jsonPath("$[0].itmsNm").value("KODEX 미국나스닥100"))
                .andExpect(jsonPath("$[0].clpr").value(22940));
    }
}
