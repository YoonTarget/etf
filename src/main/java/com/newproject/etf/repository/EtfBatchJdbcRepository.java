package com.newproject.etf.repository;

import com.newproject.etf.entity.EtfEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EtfBatchJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public EtfBatchJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<EtfEntity> entities) {
        String sql = "INSERT INTO etf_entity (flt_rt, nav, mkp, hipr, lopr, trqu, tr_prc, mrkt_tot_amt, n_ppt_tot_amt, st_lstg_cnt, bss_idx_idx_nm, bss_idx_clpr, bas_dt, srtn_cd, isin_cd, itms_nm, clpr, vs) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, entities, 1000, (ps, entity) -> {
            ps.setBigDecimal(1, entity.getFltRt());
            ps.setBigDecimal(2, entity.getNav());
            ps.setBigDecimal(3, entity.getMkp());
            ps.setBigDecimal(4, entity.getHipr());
            ps.setBigDecimal(5, entity.getLopr());
            ps.setLong(6, entity.getTrqu());
            ps.setLong(7, entity.getTrPrc());
            ps.setLong(8, entity.getMrktTotAmt());
            ps.setLong(9, entity.getNPptTotAmt());
            ps.setLong(10, entity.getStLstgCnt());
            ps.setString(11, entity.getBssIdxIdxNm());
            ps.setBigDecimal(12, entity.getBssIdxClpr());
            ps.setString(13, entity.getBasDt());
            ps.setString(14, entity.getSrtnCd());
            ps.setString(15, entity.getIsinCd());
            ps.setString(16, entity.getItmsNm());
            ps.setBigDecimal(17, entity.getClpr());
            ps.setBigDecimal(18, entity.getVs());
        });
    }
}