package com.newproject.etf.service;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.dto.EtfRiskBadgeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EtfRiskBadgeService {

    private static final BigDecimal HIGH_VOLATILITY_THRESHOLD = new BigDecimal("2.0");
    private static final long LOW_VOLUME_THRESHOLD = 10_000L;
    private static final int VOLATILITY_LOOKBACK_DAYS = 20;

    public List<EtfRiskBadgeDto> evaluate(List<EtfDto> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        EtfDto latest = history.get(history.size() - 1);
        List<EtfRiskBadgeDto> badges = new ArrayList<>();
        String itemName = latest.getItmsNm() == null ? "" : latest.getItmsNm();

        if (itemName.contains("레버리지")) {
            badges.add(new EtfRiskBadgeDto(
                    "leverage",
                    "레버리지",
                    "기초지수 변동을 확대 추종하므로 초보자는 구조를 먼저 확인하는 편이 좋습니다.",
                    "danger"
            ));
        }

        if (itemName.contains("인버스")) {
            badges.add(new EtfRiskBadgeDto(
                    "inverse",
                    "인버스",
                    "지수 하락에 베팅하는 구조라 일반적인 장기 적립 투자와 성격이 다릅니다.",
                    "danger"
            ));
        }

        if (isHighlyVolatile(history)) {
            badges.add(new EtfRiskBadgeDto(
                    "high-volatility",
                    "고변동 가능",
                    "최근 등락폭이 큰 편이라 짧은 기간에도 손익 변동이 클 수 있습니다.",
                    "warning"
            ));
        }

        if (isLowVolume(latest)) {
            badges.add(new EtfRiskBadgeDto(
                    "low-volume",
                    "거래량 적음",
                    "거래량이 적으면 원하는 가격에 매수·매도하기 어려울 수 있습니다.",
                    "secondary"
            ));
        }

        return badges;
    }

    private boolean isHighlyVolatile(List<EtfDto> history) {
        int start = Math.max(0, history.size() - VOLATILITY_LOOKBACK_DAYS);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        for (int i = start; i < history.size(); i++) {
            BigDecimal value = parseBigDecimal(history.get(i).getFltRt());
            if (value == null) {
                continue;
            }
            sum = sum.add(value.abs());
            count++;
        }

        if (count == 0) {
            return false;
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(count), 4, java.math.RoundingMode.HALF_UP);
        return average.compareTo(HIGH_VOLATILITY_THRESHOLD) >= 0;
    }

    private boolean isLowVolume(EtfDto latest) {
        Long volume = parseLong(latest.getTrqu());
        return volume != null && volume < LOW_VOLUME_THRESHOLD;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
