package com.newproject.etf.controller;

import com.newproject.etf.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/openDataApi")
public class EtfController {
    private final EtfService etfService;

    @Autowired
    public EtfController(EtfService etfService) {
        this.etfService = etfService;
    }

    @ResponseBody
    @GetMapping(value = "/{apiName}")
    public String getPriceInfo(Model model, @PathVariable("apiName") String apiName, @RequestParam Map<String, String> queryParams) {
        return etfService.list(apiName, queryParams);
    }
}
