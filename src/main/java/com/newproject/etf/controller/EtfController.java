package com.newproject.etf.controller;

import com.newproject.etf.service.EtfService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/openDataApi")
public class EtfController {
    private final EtfService etfService;
    private final String url = "https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService";
    private final String serviceKey = "BBDYHxpLb5iDQfFrXs95dcZqTnYTBG%2B%2Bo6bPr0BC9bmIHnG5gB48wToN04d4DM8uRSj7m5ha1mQvRdLJ%2Fpss9Q%3D%3D";

    public EtfController(EtfService etfService) {
        this.etfService = etfService;
    }

    @GetMapping("/getETFPriceInfo")
    public String getETFPriceInfo(Model model, @RequestParam String endPoint) {
        model.addAttribute("resultList", etfService.list(endPoint));
        return "etfPriceInfo";
    }

    @GetMapping("/getETNPriceInfo")
    public String getETNPriceInfo(Model model, @RequestParam String endPoint) {
        model.addAttribute("resultList", etfService.list(endPoint));
        return "etnPriceInfo";
    }

    @GetMapping("/getELWPriceInfo")
    public String getELWPriceInfo(Model model, @RequestParam String endPoint) {
        model.addAttribute("resultList", etfService.list(endPoint));
        return "etwPriceInfo";
    }
}
