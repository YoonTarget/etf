package com.newproject.etf.service;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.entity.EtfPriceInfoId;
import com.newproject.etf.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EtfService {
    private final EtfRepository etfRepository;

    // === 기본 CRUD 메서드들 ===

    /**
     * 모든 ETF 데이터 조회
     */
    public List<EtfEntity> getAllEtfData() {
        log.debug("모든 ETF 데이터 조회");
        return etfRepository.findAll();
    }

    /**
     * 페이징된 ETF 데이터 조회
     */
    public Page<EtfEntity> getAllEtfDataPaged(Pageable pageable) {
        log.debug("페이징된 ETF 데이터 조회: {}", pageable);
        return etfRepository.findAll(pageable);
    }

    /**
     * 복합키로 특정 ETF 데이터 조회
     */
    public Optional<EtfEntity> getEtfById(String basDt, String itmsNm) {
        log.debug("ETF 데이터 조회: 날짜={}, 종목명={}", basDt, itmsNm);
        EtfPriceInfoId id = new EtfPriceInfoId(basDt, itmsNm);
        return etfRepository.findById(id);
    }

    /**
     * 단일 ETF 데이터 저장
     */
    @Transactional
    public EtfEntity saveEtfData(EtfEntity etfEntity) {
        log.debug("ETF 데이터 저장: {}", etfEntity.getItmsNm());
        return etfRepository.save(etfEntity);
    }

    /**
     * 여러 ETF 데이터 배치 저장
     */
    @Transactional
    public List<EtfEntity> saveAllEtfData(List<EtfEntity> etfEntities) {
        log.info("ETF 데이터 배치 저장: {} 건", etfEntities.size());
        return etfRepository.saveAll(etfEntities);
    }

    /**
     * ETF 데이터 삭제 (복합키)
     */
    @Transactional
    public void deleteEtfData(String basDt, String itmsNm) {
        log.debug("ETF 데이터 삭제: 날짜={}, 종목명={}", basDt, itmsNm);
        EtfPriceInfoId id = new EtfPriceInfoId(basDt, itmsNm);
        etfRepository.deleteById(id);
    }

    /**
     * ETF 데이터 존재 여부 확인
     */
    public boolean existsEtfData(String basDt, String itmsNm) {
        log.debug("ETF 데이터 존재 확인: 날짜={}, 종목명={}", basDt, itmsNm);
        return etfRepository.existsByBasDtAndItmsNm(basDt, itmsNm);
    }

    /**
     * 전체 ETF 데이터 개수 조회
     */
    public long getTotalEtfCount() {
        return etfRepository.count();
    }

    // === 비즈니스 로직 메서드들 ===

    /**
     * 특정 날짜의 모든 ETF 데이터 조회
     */
    public List<EtfEntity> getEtfDataByDate(String basDt) {
        log.debug("특정 날짜 ETF 데이터 조회: {}", basDt);
        return etfRepository.findByBasDt(basDt);
    }

    /**
     * 특정 날짜 범위의 ETF 데이터 조회
     */
    public List<EtfEntity> getEtfDataByDateRange(String startDate, String endDate) {
        log.debug("날짜 범위 ETF 데이터 조회: {} ~ {}", startDate, endDate);
        return etfRepository.findByDateRange(startDate, endDate);
    }

    /**
     * 종목명으로 ETF 검색 (부분 일치)
     */
    public List<EtfEntity> searchEtfByName(String keyword) {
        log.debug("ETF 종목명 검색: {}", keyword);
        return etfRepository.findByItmsNmContaining(keyword);
    }

    /**
     * 특정 종목의 모든 이력 데이터 조회 (최신순)
     */
    public List<EtfEntity> getEtfHistoryByName(String itmsNm) {
        log.debug("ETF 종목 이력 조회: {}", itmsNm);
        return etfRepository.findByItmsNmOrderByBasDtDesc(itmsNm);
    }

    /**
     * 특정 날짜의 고수익 ETF 조회 (등락률 기준)
     */
    public List<EtfEntity> getHighPerformingEtfs(String basDt, BigDecimal minFltRt) {
        log.debug("고수익 ETF 조회: 날짜={}, 최소등락률={}", basDt, minFltRt);
        return etfRepository.findByDateRange(basDt, basDt)
                .stream()
                .filter(etf -> etf.getFltRt() != null && etf.getFltRt().compareTo(minFltRt) >= 0)
                .toList();
    }

    /**
     * 특정 날짜의 거래량 상위 ETF 조회
     */
    public List<EtfEntity> getTopTradingVolumeEtfs(String basDt) {
        log.debug("거래량 상위 ETF 조회: {}", basDt);
        return etfRepository.findTopTradingVolumeByDate(basDt);
    }

    /**
     * 특정 날짜의 등락률 순위 조회 (상위순)
     */
    public List<EtfEntity> getEtfRankingByReturn(String basDt) {
        log.debug("등락률 순위 조회: {}", basDt);
        return etfRepository.findByDateOrderByFltRtDesc(basDt);
    }

    /**
     * 특정 기초지수 관련 ETF 조회
     */
    public List<EtfEntity> getEtfsByIndexName(String indexName) {
        log.debug("기초지수별 ETF 조회: {}", indexName);
        return etfRepository.findByIndexName(indexName);
    }

    /**
     * 특정 날짜의 시가총액 통계 정보 조회
     */
    public MarketCapStats getMarketCapStats(String basDt) {
        log.debug("시가총액 통계 조회: {}", basDt);
        Object[] result = etfRepository.getMarketCapStatsByDate(basDt);

        if (result.length >= 3) {
            Long totalMarketCap = result[0] != null ? ((Number) result[0]).longValue() : 0L;
            Double avgMarketCap = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            Long count = result[2] != null ? ((Number) result[2]).longValue() : 0L;

            return new MarketCapStats(totalMarketCap, avgMarketCap, count);
        }

        return new MarketCapStats(0L, 0.0, 0L);
    }

    /**
     * 최근 거래일 목록 조회
     */
    public List<String> getRecentTradingDates(int limit) {
        log.debug("최근 거래일 조회: {} 개", limit);
        return etfRepository.findRecentTradingDates(limit);
    }

    /**
     * 특정 날짜의 데이터 개수 조회
     */
    public long getEtfCountByDate(String basDt) {
        log.debug("특정 날짜 데이터 개수 조회: {}", basDt);
        return etfRepository.countByBasDt(basDt);
    }

    /**
     * 특정 조건을 만족하는 ETF 개수 조회
     */
    public long getEtfCountByConditions(String basDt, BigDecimal minRate, Long minVolume) {
        log.debug("조건별 ETF 개수 조회: 날짜={}, 최소등락률={}, 최소거래량={}",
                basDt, minRate, minVolume);
        return etfRepository.countByConditions(basDt, minRate, minVolume);
    }

    // === 데이터 관리 메서드들 ===

    /**
     * 특정 날짜의 모든 데이터 삭제
     */
    @Transactional
    public void deleteEtfDataByDate(String basDt) {
        log.info("특정 날짜 ETF 데이터 삭제: {}", basDt);
        etfRepository.deleteByBasDt(basDt);
    }

    /**
     * 특정 종목의 모든 이력 데이터 삭제
     */
    @Transactional
    public void deleteEtfDataByName(String itmsNm) {
        log.info("특정 종목 ETF 데이터 삭제: {}", itmsNm);
        etfRepository.deleteByItmsNm(itmsNm);
    }

    /**
     * 모든 ETF 데이터 삭제 (주의!)
     */
    @Transactional
    public void deleteAllEtfData() {
        log.warn("모든 ETF 데이터 삭제 실행");
        etfRepository.deleteAll();
    }

    /**
     * 가장 최근 날짜의 모든 ETF 데이터 조회
     */
    // ✅ "etfs"라는 이름의 캐시를 사용
    @Cacheable(value = "etfs")
    public List<EtfEntity> getRecentEtfData() {
        return etfRepository.findLatestEtfData();
    }

    /**
     * "etfs" 캐시 저장소의 모든 데이터를 삭제합니다.
     * 이 메서드는 배치 작업이 성공적으로 완료된 직후 호출되어야 합니다.
     */
    @CacheEvict(value = "etfs", allEntries = true)
    public void invalidateEtfCache() {
        log.info("✅ ETF 캐시 데이터가 성공적으로 초기화되었습니다.");
    }

    // === 내부 클래스: 통계 데이터 ===

    public static class MarketCapStats {
        private final Long totalMarketCap;
        private final Double avgMarketCap;
        private final Long count;

        public MarketCapStats(Long totalMarketCap, Double avgMarketCap, Long count) {
            this.totalMarketCap = totalMarketCap;
            this.avgMarketCap = avgMarketCap;
            this.count = count;
        }

        public Long getTotalMarketCap() { return totalMarketCap; }
        public Double getAvgMarketCap() { return avgMarketCap; }
        public Long getCount() { return count; }

        @Override
        public String toString() {
            return String.format("MarketCapStats{total=%d, avg=%.2f, count=%d}",
                    totalMarketCap, avgMarketCap, count);
        }
    }
}
