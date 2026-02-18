package com.newproject.etf.controller;

import com.newproject.etf.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/auto")
    public ResponseEntity<String> autoTagging() {
        tagService.autoTagging();
        return ResponseEntity.ok("Auto-tagging completed successfully.");
    }
}