package com.newproject.etf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class EtfController {
    @GetMapping("/etf")
    public String etf(Model model) {
        model.addAttribute("etfName", "Vanguard S&P 500 ETF");
        List<String> etfList = Arrays.asList("VOO", "QQQ", "SPY");
        model.addAttribute("etfList", etfList);
        return "etf";
    }
}
