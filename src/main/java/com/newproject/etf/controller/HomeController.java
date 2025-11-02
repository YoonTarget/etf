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

}
