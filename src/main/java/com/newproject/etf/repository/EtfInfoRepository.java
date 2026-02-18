package com.newproject.etf.repository;

import com.newproject.etf.entity.EtfInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtfInfoRepository extends JpaRepository<EtfInfo, String> {
    // 필요한 경우 추가적인 쿼리 메소드 정의
}