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
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    // 양방향 매핑 (선택 사항이지만, 태그로 ETF를 조회할 때 유용함)
    @OneToMany(mappedBy = "tag")
    private List<EtfTag> etfTags = new ArrayList<>();

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}