package com.newproject.etf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "etf-list"; // templates/etf-list.html을 반환
    }
}
