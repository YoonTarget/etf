package com.newproject.etf.controller;

import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/etf")
public class EtfController {
    private final EtfService etfService;

    @GetMapping("/recent")
    public List<EtfEntity> getRecentEtfData() {
        return etfService.getRecentEtfData();
    }

    @GetMapping("/{date}")
    public List<EtfEntity> getEtfByDate(@PathVariable String date) {
        return etfService.getEtfDataByDate(date);
    }

    @GetMapping("/{date}/{name}")
    public Optional<EtfEntity> getEtf(@PathVariable String date, @PathVariable String name) {
        return etfService.getEtfById(date, name);
    }
}
