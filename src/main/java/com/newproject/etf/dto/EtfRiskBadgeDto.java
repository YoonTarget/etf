package com.newproject.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EtfRiskBadgeDto {
    private final String code;
    private final String label;
    private final String description;
    private final String severity;
}
