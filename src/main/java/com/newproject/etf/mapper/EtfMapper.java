package com.newproject.etf.mapper;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import org.springframework.stereotype.Component;

@Component
public class EtfMapper {
    public static EtfDto toDto(EtfEntity entity) {
        return new EtfDto(
                entity.getBasDt(),
                entity.getSrtnCd(),
                entity.getIsinCd(),
                entity.getItmsNm(),
                entity.getClpr() != null ? entity.getClpr().toString() : null,
                entity.getVs() != null ? entity.getVs().toString() : null,
                entity.getFltRt() != null ? entity.getFltRt().toString() : null,
                entity.getNav() != null ? entity.getNav().toString() : null,
                entity.getMkp() != null ? entity.getMkp().toString() : null,
                entity.getHipr() != null ? entity.getHipr().toString() : null,
                entity.getLopr() != null ? entity.getLopr().toString() : null,
                entity.getTrqu() != null ? entity.getTrqu().toString() : null,
                entity.getTrPrc() != null ? entity.getTrPrc().toString() : null,
                entity.getMrktTotAmt() != null ? entity.getMrktTotAmt().toString() : null,
                entity.getStLstgCnt() != null ? entity.getStLstgCnt().toString() : null,
                entity.getBssIdxIdxNm(),
                entity.getBssIdxClpr() != null ? entity.getBssIdxClpr().toString() : null,
                entity.getNPptTotAmt() != null ? entity.getNPptTotAmt().toString() : null
        );
    }
}