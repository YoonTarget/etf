package com.newproject.etf.controller;

import com.newproject.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EtfService etfService;

    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html을 반환하도록 수정
    }

}
