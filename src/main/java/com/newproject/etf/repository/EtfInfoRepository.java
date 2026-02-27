package com.newproject.etf.repository;

import com.newproject.etf.entity.EtfInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtfInfoRepository extends JpaRepository<EtfInfo, String> {

    @Query("SELECT DISTINCT i " +
           "FROM EtfInfo i " +
           "JOIN i.etfTags et " +
           "JOIN et.tag t " +
           "WHERE t.tagName = :tagName")
    List<EtfInfo> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @EntityGraph(attributePaths = {"etfTags", "etfTags.tag"})
    @Query("SELECT DISTINCT i FROM EtfInfo i")
    List<EtfInfo> findAllWithTags();

    @Query("SELECT new com.newproject.etf.dto.TagDto(t.id, t.tagName, COUNT(et)) " +
           "FROM Tag t " +
           "LEFT JOIN t.etfTags et " +
           "GROUP BY t.id, t.tagName " +
           "ORDER BY COUNT(et) DESC")
    List<com.newproject.etf.dto.TagDto> findAllTagsWithCount();
}
