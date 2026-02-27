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

    interface EtfSummaryRow {
        String getSrtnCd();
        String getItmsNm();
        BigDecimal getClpr();
        BigDecimal getFltRt();
        BigDecimal getVs();
        Long getTrqu();
        Long getMrktTotAmt();
    }

    List<EtfEntity> findByBasDt(String basDt);

    List<EtfEntity> findBySrtnCdOrderByBasDtAsc(String srtnCd);

    List<EtfEntity> findByItmsNmContaining(String itmsNm);

    Optional<EtfEntity> findByBasDtAndItmsNm(String basDt, String itmsNm);

    boolean existsByBasDtAndItmsNm(String basDt, String itmsNm);

    long countByBasDt(String basDt);

    void deleteByBasDt(String basDt);

    void deleteByItmsNm(String itmsNm);

    List<EtfEntity> findByFltRtGreaterThanEqual(BigDecimal minFltRt);

    List<EtfEntity> findByTrquGreaterThanEqual(Long minTrqu);

    List<EtfEntity> findByItmsNmOrderByBasDtDesc(String itmsNm);

    @Query("SELECT e FROM EtfEntity e WHERE e.basDt BETWEEN :startDate AND :endDate ORDER BY e.basDt DESC, e.itmsNm ASC")
    List<EtfEntity> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = :basDt AND e.trqu IS NOT NULL ORDER BY e.trqu DESC")
    List<EtfEntity> findTopTradingVolumeByDate(@Param("basDt") String basDt);

    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = :basDt AND e.fltRt IS NOT NULL ORDER BY e.fltRt DESC")
    List<EtfEntity> findByDateOrderByFltRtDesc(@Param("basDt") String basDt);

    @Query("SELECT e FROM EtfEntity e WHERE e.bssIdxIdxNm LIKE %:indexName% ORDER BY e.basDt DESC")
    List<EtfEntity> findByIndexName(@Param("indexName") String indexName);

    @Query("SELECT SUM(e.mrktTotAmt), AVG(e.mrktTotAmt), COUNT(e) FROM EtfEntity e WHERE e.basDt = :basDt AND e.mrktTotAmt IS NOT NULL")
    Object[] getMarketCapStatsByDate(@Param("basDt") String basDt);

    @Query(value = "SELECT DISTINCT bas_dt FROM etf_price_info ORDER BY bas_dt DESC LIMIT :limit", nativeQuery = true)
    List<String> findRecentTradingDates(@Param("limit") int limit);

    @Query("SELECT COUNT(e) FROM EtfEntity e WHERE e.basDt = :basDt AND e.fltRt >= :minRate AND e.trqu >= :minVolume")
    long countByConditions(@Param("basDt") String basDt,
                           @Param("minRate") BigDecimal minRate,
                           @Param("minVolume") Long minVolume);

    @Query("SELECT e FROM EtfEntity e WHERE e.basDt = (SELECT MAX(e2.basDt) FROM EtfEntity e2) ORDER BY e.trqu DESC")
    List<EtfEntity> findLatestEtfData();

    @Query("SELECT MAX(e.basDt) FROM EtfEntity e")
    String findMaxBasDt();

    Optional<EtfEntity> findTopBySrtnCdOrderByBasDtDesc(String srtnCd);

    @Query("SELECT e FROM EtfEntity e " +
           "WHERE e.srtnCd IN :srtnCds " +
           "AND e.basDt = (SELECT MAX(e2.basDt) FROM EtfEntity e2 WHERE e2.srtnCd = e.srtnCd)")
    List<EtfEntity> findLatestBySrtnCdIn(@Param("srtnCds") List<String> srtnCds);

    @Query(value = """
            SELECT
              i.srtn_cd AS srtnCd,
              i.itms_nm AS itmsNm,
              p.clpr AS clpr,
              p.flt_rt AS fltRt,
              p.vs AS vs,
              p.trqu AS trqu,
              p.mrkt_tot_amt AS mrktTotAmt
            FROM etf_info i
            JOIN etf_tag et ON et.srtn_cd = i.srtn_cd
            JOIN tag t ON t.tag_id = et.tag_id
            LEFT JOIN LATERAL (
              SELECT e.clpr, e.flt_rt, e.vs, e.trqu, e.mrkt_tot_amt
              FROM etf_price_info e
              WHERE e.srtn_cd = i.srtn_cd
              ORDER BY e.bas_dt DESC
              LIMIT 1
            ) p ON true
            WHERE t.tag_name = :tagName
            ORDER BY COALESCE(p.trqu, 0) DESC, i.srtn_cd
            LIMIT :limit
            """, nativeQuery = true)
    List<EtfSummaryRow> findEtfSummariesByTag(@Param("tagName") String tagName, @Param("limit") int limit);

    @Query(value = """
            SELECT
              i.srtn_cd AS srtnCd,
              i.itms_nm AS itmsNm,
              p.clpr AS clpr,
              p.flt_rt AS fltRt,
              p.vs AS vs,
              p.trqu AS trqu,
              p.mrkt_tot_amt AS mrktTotAmt
            FROM etf_info i
            LEFT JOIN LATERAL (
              SELECT e.clpr, e.flt_rt, e.vs, e.trqu, e.mrkt_tot_amt
              FROM etf_price_info e
              WHERE e.srtn_cd = i.srtn_cd
              ORDER BY e.bas_dt DESC
              LIMIT 1
            ) p ON true
            WHERE
              UPPER(i.itms_nm) LIKE UPPER(CONCAT('%', :query, '%'))
              OR EXISTS (
                SELECT 1
                FROM etf_tag et
                JOIN tag t ON t.tag_id = et.tag_id
                WHERE et.srtn_cd = i.srtn_cd
                  AND UPPER(t.tag_name) LIKE UPPER(CONCAT('%', :query, '%'))
              )
            ORDER BY COALESCE(p.trqu, 0) DESC, i.srtn_cd
            LIMIT :limit
            """, nativeQuery = true)
    List<EtfSummaryRow> findEtfSummariesByKeyword(@Param("query") String query, @Param("limit") int limit);
}
