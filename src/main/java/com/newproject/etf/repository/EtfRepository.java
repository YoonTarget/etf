package com.newproject.etf.repository;

import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.entity.EtfPriceInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EtfRepository extends JpaRepository<EtfEntity, EtfPriceInfoId> {
    // === 기본 조회 메서드들 (메서드 이름 규칙 사용) ===

    /**
     * 특정 날짜의 모든 ETF 데이터 조회
     */
    List<EtfEntity> findByBasDt(String basDt);

    /**
     * 특정 단축코드의 모든 ETF 데이터 조회 (날짜 오름차순)
     */
    List<EtfEntity> findBySrtnCdOrderByBasDtAsc(String srtnCd);

    /**
     * 특정 종목명을 포함하는 ETF 데이터 조회
     */
    List<EtfEntity> findByItmsNmContaining(String itmsNm);

    /**
     * 특정 날짜와 종목명으로 ETF 데이터 조회
     */
    Optional<EtfEntity> findByBasDtAndItmsNm(String basDt, String itmsNm);

    /**
     * 특정 날짜와 종목명 조합이 존재하는지 확인
     */
    boolean existsByBasDtAndItmsNm(String basDt, String itmsNm);

    /**
     * 특정 날짜의 데이터 개수 조회
     */
    long countByBasDt(String basDt);

    /**
     * 특정 날짜의 모든 ETF 데이터 삭제
     */
    void deleteByBasDt(String basDt);

    /**
     * 특정 종목의 모든 이력 데이터 삭제
     */
    void deleteByItmsNm(String itmsNm);

    /**
     * 특정 등락률 이상의 ETF 조회
     */
    List<EtfEntity> findByFltRtGreaterThanEqual(BigDecimal minFltRt);

    /**
     * 특정 거래량 이상의 ETF 조회
     */
    List<EtfEntity> findByTrquGreaterThanEqual(Long minTrqu);

    /**
     * 날짜 순으로 정렬된 특정 종목 데이터 조회
     */
    List<EtfEntity> findByItmsNmOrderByBasDtDesc(String itmsNm);

    // === 복잡한 쿼리들 (@Query 어노테이션 사용) ===

    /**
     * 특정 날짜 범위의 ETF 데이터 조회
     */
    @Query("SELECT e FROM EtfEntity e WHERE e.basDt BETWEEN :startDate AND :endDate ORDER BY e.basDt DESC, e.itmsNm ASC")
    List<EtfEntity> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 특정 날짜의 상위 거래량 ETF 조회
     */
    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = :basDt AND e.trqu IS NOT NULL ORDER BY e.trqu DESC")
    List<EtfEntity> findTopTradingVolumeByDate(@Param("basDt") String basDt);

    /**
     * 특정 날짜의 등락률 상위/하위 ETF 조회
     */
    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = :basDt AND e.fltRt IS NOT NULL ORDER BY e.fltRt DESC")
    List<EtfEntity> findByDateOrderByFltRtDesc(@Param("basDt") String basDt);

    /**
     * 특정 기초지수의 ETF들 조회
     */
    @Query("SELECT e FROM EtfEntity e WHERE e.bssIdxIdxNm LIKE %:indexName% ORDER BY e.basDt DESC")
    List<EtfEntity> findByIndexName(@Param("indexName") String indexName);

    /**
     * 특정 날짜의 시가총액 통계
     */
    @Query("SELECT SUM(e.mrktTotAmt), AVG(e.mrktTotAmt), COUNT(e) FROM EtfEntity e WHERE e.basDt = :basDt AND e.mrktTotAmt IS NOT NULL")
    Object[] getMarketCapStatsByDate(@Param("basDt") String basDt);

    /**
     * 최근 거래일 조회 (Native Query)
     */
    @Query(value = "SELECT DISTINCT bas_dt FROM etf_price_info ORDER BY bas_dt DESC LIMIT :limit", nativeQuery = true)
    List<String> findRecentTradingDates(@Param("limit") int limit);

    /**
     * 특정 조건의 ETF 개수 조회
     */
    @Query("SELECT COUNT(e) FROM EtfEntity e WHERE e.basDt = :basDt AND e.fltRt >= :minRate AND e.trqu >= :minVolume")
    long countByConditions(@Param("basDt") String basDt,
                           @Param("minRate") BigDecimal minRate,
                           @Param("minVolume") Long minVolume);

    /**
     * 가장 최근 날짜의 모든 ETF 데이터 조회(거래량 높은 순으로 정렬)
     */
    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = (SELECT MAX(e2.basDt) FROM EtfEntity e2) ORDER BY e.trqu DESC")
    List<EtfEntity> findLatestEtfData();

    /**
     * 가장 최근 기준일자 조회
     */
    @Query("SELECT MAX(e.basDt) FROM EtfEntity e")
    String findMaxBasDt();
}