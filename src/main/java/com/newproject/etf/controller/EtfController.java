package com.newproject.etf.controller;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/openDataApi")
public class EtfController {
    private final EtfService etfService;
    private final String url = "https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService";
    private final String serviceKey = "BBDYHxpLb5iDQfFrXs95dcZqTnYTBG%2B%2Bo6bPr0BC9bmIHnG5gB48wToN04d4DM8uRSj7m5ha1mQvRdLJ%2Fpss9Q%3D%3D";

    @Autowired
    public EtfController(EtfService etfService) {
        this.etfService = etfService;
    }

    @GetMapping(value = "/{apiName}", produces = "application/json")
    public String getPriceInfo(Model model, @PathVariable("apiName") String apiName) {
        model.addAttribute("resultList", etfService.list(url, serviceKey, apiName));
        return "etf";
    }
}
