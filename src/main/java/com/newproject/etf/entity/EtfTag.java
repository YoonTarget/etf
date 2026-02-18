package com.newproject.etf.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "etf_tag")
public class EtfTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etf_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "srtn_cd")
    private EtfInfo etfInfo; // Etf -> EtfInfo로 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public EtfTag(EtfInfo etfInfo, Tag tag) {
        this.etfInfo = etfInfo;
        this.tag = tag;
    }
}