package com.newproject.etf.repository;

import com.newproject.etf.entity.EtfInfo;
import com.newproject.etf.entity.EtfTag;
import com.newproject.etf.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtfTagRepository extends JpaRepository<EtfTag, Long> {
    boolean existsByEtfInfoAndTag(EtfInfo etfInfo, Tag tag);
}