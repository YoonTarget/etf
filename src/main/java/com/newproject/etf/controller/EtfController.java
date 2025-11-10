package com.newproject.etf.controller;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.dto.EtfInfoView;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.service.EtfService;
import com.newproject.etf.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.newproject.etf.util.ViewFormatters.formatBasDt;
import static com.newproject.etf.util.ViewFormatters.formatWon;

@Controller
@RequiredArgsConstructor
@RequestMapping("/etf")
public class EtfController {
    private final EtfService etfService;
    private final NewsService newsService;

    @GetMapping("/recent")
    @ResponseBody
    public List<EtfDto> getRecentEtfData() {
        return etfService.getRecentEtfData();
    }

    @GetMapping("/{srtnCd}")
    public String etfDetail(Model model, @PathVariable String srtnCd) throws UnsupportedEncodingException {
        List<EtfDto> etfDetails = etfService.getAllEtfDataOfSrtnCd(srtnCd);

        if (etfDetails.isEmpty()) {
            return "error-page"; // 데이터 없을 때 처리
        }

        EtfDto e = etfDetails.get(etfDetails.size() - 1); // 최신 데이터 기준
        EtfInfoView etfInfo = new EtfInfoView(
                e.getItmsNm(), // 종목명
                e.getIsinCd(), // ISIN 코드
                e.getBssIdxIdxNm(), // 기초지수명
                e.getBssIdxClpr(), // 기초지수 종가
                formatWon(Long.valueOf(e.getMrktTotAmt())), // 시가총액
                formatWon(BigDecimal.valueOf(Double.parseDouble(e.getNav()))), // 순자산가치
                formatBasDt(etfDetails.get(0).getBasDt()) // 상장일
        );

        model.addAttribute("etfInfo", etfInfo);
        model.addAttribute("etfDetails", etfDetails);
        model.addAttribute("news", newsService.getNewsByQuery(e.getBssIdxIdxNm()));

        return "etf-detail";
    }

    @GetMapping("/{date}/{name}")
    public Optional<EtfEntity> getEtf(@PathVariable String date, @PathVariable String name) {
        return etfService.getEtfById(date, name);
    }
}
