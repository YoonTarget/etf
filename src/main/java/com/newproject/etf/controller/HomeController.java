package com.newproject.etf.controller;

import com.newproject.etf.service.BatchMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BatchMetadataService batchMetadataService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestBasDtLabel", batchMetadataService.getLatestBasDtLabel());
        model.addAttribute("lastSuccessfulBatchAtLabel", batchMetadataService.getLastSuccessfulBatchAtLabel());
        return "index";
    }
}
