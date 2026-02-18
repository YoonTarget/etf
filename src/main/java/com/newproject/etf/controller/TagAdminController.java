package com.newproject.etf.controller;

import com.newproject.etf.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class TagAdminController {

    private final TagService tagService;

    @PostMapping("/auto-tagging")
    public ResponseEntity<String> runAutoTagging() {
        tagService.autoTagging();
        return ResponseEntity.ok("Auto-tagging process started successfully.");
    }
}