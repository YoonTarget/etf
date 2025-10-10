package com.newproject.etf.controller;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.dto.EtfInfoView;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.service.EtfService;
import com.newproject.etf.util.ViewFormatters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.newproject.etf.util.ViewFormatters.formatBasDt;
import static com.newproject.etf.util.ViewFormatters.formatWon;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EtfService etfService;

    @GetMapping("/")
    public String home() {
        return "etf-list"; // templates/etf-list.html을 반환
    }

    @GetMapping("/etf-detail/{srtnCd}")
    public String etfDetail(Model model, @PathVariable String srtnCd) {
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

        return "etf-detail";
    }

}
