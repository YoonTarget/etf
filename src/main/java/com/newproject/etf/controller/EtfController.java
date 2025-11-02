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

        /*
        // 상장일 = basDt 최솟값
        String listedDate = etfDetails.stream()
                .map(EtfDto::getBasDt)
                .min(Comparator.naturalOrder())
                .orElse(null);
         */

        /*
        Map<String, Object> etfInfo = Map.of(
                "itmsNm", etfDetails.get(0).getItmsNm(), // 종목명
                "isinCd", etfDetails.get(0).getIsinCd(), // ISIN 코드
                "basDt", etfDetails.get(0).getBasDt(), // 상장일
                "nav", etfDetails.get(0).getNav(), // 순자산가치
                "mrktTotAmt", etfDetails.get(0).getMrktTotAmt() // 시가총액
        );
        */
        EtfDto e = etfDetails.get(0);
        EtfInfoView etfInfo = new EtfInfoView(
                e.getItmsNm(),
                e.getIsinCd(),
                formatBasDt(e.getBasDt()),
                formatWon(BigDecimal.valueOf(Double.parseDouble(e.getNav()))),
                formatWon(Long.valueOf(e.getMrktTotAmt()))
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
