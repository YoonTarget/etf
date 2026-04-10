package com.newproject.etf.service;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.dto.EtfRiskBadgeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EtfRiskBadgeServiceTest {

    private final EtfRiskBadgeService etfRiskBadgeService = new EtfRiskBadgeService();

    @Test
    @DisplayName("레버리지와 거래량 부족 배지를 부여한다")
    void evaluateLeverageAndLowVolume() {
        List<EtfDto> history = List.of(
                dto("20260407", "KODEX 레버리지", "1.20", "9000")
        );

        List<EtfRiskBadgeDto> result = etfRiskBadgeService.evaluate(history);

        assertThat(result).extracting(EtfRiskBadgeDto::getCode)
                .contains("leverage", "low-volume");
    }

    @Test
    @DisplayName("최근 평균 절대 등락률이 높으면 고변동 배지를 부여한다")
    void evaluateHighVolatility() {
        List<EtfDto> history = List.of(
                dto("20260310", "KODEX 2차전지", "2.50", "20000"),
                dto("20260311", "KODEX 2차전지", "-2.10", "20000"),
                dto("20260312", "KODEX 2차전지", "3.20", "20000"),
                dto("20260313", "KODEX 2차전지", "-2.40", "20000")
        );

        List<EtfRiskBadgeDto> result = etfRiskBadgeService.evaluate(history);

        assertThat(result).extracting(EtfRiskBadgeDto::getCode)
                .contains("high-volatility");
    }

    @Test
    @DisplayName("인버스 배지를 부여한다")
    void evaluateInverse() {
        List<EtfDto> history = List.of(
                dto("20260407", "KODEX 인버스", "0.50", "50000")
        );

        List<EtfRiskBadgeDto> result = etfRiskBadgeService.evaluate(history);

        assertThat(result).extracting(EtfRiskBadgeDto::getCode)
                .contains("inverse");
    }

    private EtfDto dto(String basDt, String itmsNm, String fltRt, String trqu) {
        return new EtfDto(
                basDt,
                "123456",
                "KR1234567890",
                itmsNm,
                "10000",
                "100",
                fltRt,
                "10000",
                "10000",
                "10100",
                "9900",
                trqu,
                "100000000",
                "1000000000",
                "1000000",
                "지수명",
                "1000",
                "100000000"
        );
    }
}
