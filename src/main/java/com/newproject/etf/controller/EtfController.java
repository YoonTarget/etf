package com.newproject.etf.controller;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.dto.EtfInfoView;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.service.BatchMetadataService;
import com.newproject.etf.service.EtfRiskBadgeService;
import com.newproject.etf.service.EtfService;
import com.newproject.etf.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private final BatchMetadataService batchMetadataService;
    private final EtfRiskBadgeService etfRiskBadgeService;

    @GetMapping("/recent")
    @ResponseBody
    public List<EtfDto> getRecentEtfData() {
        return etfService.getRecentEtfData();
    }

    @GetMapping("/detail/{srtnCd}")
    public String etfDetail(Model model, @PathVariable String srtnCd) throws UnsupportedEncodingException {
        List<EtfDto> etfDetails = etfService.getAllEtfDataOfSrtnCd(srtnCd);

        if (etfDetails.isEmpty()) {
            return "error-page";
        }

        EtfDto e = etfDetails.get(etfDetails.size() - 1);
        EtfInfoView etfInfo = new EtfInfoView(
                e.getItmsNm(),
                e.getIsinCd(),
                e.getBssIdxIdxNm(),
                e.getBssIdxClpr(),
                formatWon(Long.valueOf(e.getMrktTotAmt())),
                formatWon(BigDecimal.valueOf(Double.parseDouble(e.getNav()))),
                formatBasDt(etfDetails.get(0).getBasDt())
        );

        model.addAttribute("etfInfo", etfInfo);
        model.addAttribute("etfDetails", etfDetails);
        model.addAttribute("news", newsService.getNewsByQuery(e.getBssIdxIdxNm()));
        model.addAttribute("latestBasDtLabel", batchMetadataService.getLatestBasDtLabel());
        model.addAttribute("lastSuccessfulBatchAtLabel", batchMetadataService.getLastSuccessfulBatchAtLabel());
        model.addAttribute("riskBadges", etfRiskBadgeService.evaluate(etfDetails));

        return "etf-detail";
    }

    @GetMapping("/{date}/{name}")
    public Optional<EtfEntity> getEtf(@PathVariable String date, @PathVariable String name) {
        return etfService.getEtfById(date, name);
    }
}
