package com.newproject.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "etf_price_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(EtfPriceInfoId.class) // 복합 키 클래스 지정
public class EtfEntity {

    @Id // basDt를 복합 키의 첫 번째 부분으로 지정
    @Column(name = "bas_dt", nullable = false)
    private String basDt; // 기준일자 (YYYYMMDD 형식)

    @Id // itmsNm을 복합 키의 두 번째 부분으로 지정
    @Column(name = "itms_nm", nullable = false)
    private String itmsNm; // 종목명

    @Column(name = "flt_rt", precision = 5, scale = 2)
    private BigDecimal fltRt; // 등락률

    @Column(name = "nav", precision = 15, scale = 2)
    private BigDecimal nav; // 순자산가치

    @Column(name = "mkp", precision = 10, scale = 0)
    private BigDecimal mkp; // 시가

    @Column(name = "hipr", precision = 10, scale = 0)
    private BigDecimal hipr; // 고가

    @Column(name = "lopr", precision = 10, scale = 0)
    private BigDecimal lopr; // 저가

    @Column(name = "trqu")
    private Long trqu; // 거래량

    @Column(name = "tr_prc")
    private Long trPrc; // 거래대금

    @Column(name = "mrkt_tot_amt")
    private Long mrktTotAmt; // 시가총액

    @Column(name = "n_ppt_tot_amt")
    private Long nPptTotAmt; // 순자산총액

    @Column(name = "st_lstg_cnt")
    private Long stLstgCnt; // 상장좌수

    @Column(name = "bss_idx_idx_nm", length = 240)
    private String bssIdxIdxNm; // 기초지수_지수명

    @Column(name = "bss_idx_clpr", precision = 10, scale = 3)
    private BigDecimal bssIdxClpr; // 기초지수_종가

    @Column(name = "srtn_cd", length = 6)
    private String srtnCd; // 단축코드

    @Column(name = "isin_cd", length = 12)
    private String isinCd; // ISIN코드

    @Column(name = "clpr", precision = 10, scale = 0)
    private BigDecimal clpr; // 종가

    @Column(name = "vs", precision = 10, scale = 0)
    private BigDecimal vs; // 대비
}