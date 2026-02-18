package com.newproject.etf.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "etf_info")
public class EtfInfo {

    @Id
    @Column(name = "srtn_cd", length = 6)
    private String srtnCd; // 단축코드 (PK)

    @Column(name = "itms_nm", nullable = false)
    private String itmsNm; // 종목명

    @Column(name = "isin_cd", length = 12)
    private String isinCd; // ISIN코드

    // 태그와의 연관관계 (다대다 해소용 엔티티 연결)
    @OneToMany(mappedBy = "etfInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EtfTag> etfTags = new ArrayList<>();

    public EtfInfo(String srtnCd, String itmsNm, String isinCd) {
        this.srtnCd = srtnCd;
        this.itmsNm = itmsNm;
        this.isinCd = isinCd;
    }

    // 태그 추가 편의 메소드
    public void addTag(Tag tag) {
        EtfTag etfTag = new EtfTag(this, tag);
        this.etfTags.add(etfTag);
    }
}