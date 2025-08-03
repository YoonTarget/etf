package com.newproject.etf.config;

import com.newproject.etf.batch.EtfApiPagingReader; // 새롭게 만든 Reader 임포트
import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.listener.EtfJobCompletionNotificationListener;
import com.newproject.etf.service.EtfApiService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EtfBatchConfig {

    public static final String JOB_NAME = "importEtfDataJob";
    private static final int API_PAGE_SIZE = 10000; // API에서 한 번에 가져올 최대 건수

    private final EtfApiService etfApiService;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;


    // 1. ItemReader: API에서 데이터를 페이지별로 읽어오는 부분
    @Bean
    @StepScope // Step 실행 시점에 생성되도록 StepScope 사용
    public ItemReader<EtfDto> etfApiPagingReader(
            @Value("#{jobParameters[targetDate]}") String targetDateString) {

        // Job Parameter에서 받은 날짜를 LocalDate로 파싱하거나, 없으면 오늘 날짜 사용
        LocalDate actualTargetDate;
        if (targetDateString != null && !targetDateString.isEmpty()) {
            actualTargetDate = LocalDate.parse(targetDateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            System.out.println("[EtfBatchConfig] Initializing etfApiPagingReader for date from JobParameter: " + actualTargetDate);
        } else {
            actualTargetDate = LocalDate.now(); // Job Parameter가 없으면 오늘 날짜 사용 (권장하지 않음, 파라미터 필수)
            System.out.println("[EtfBatchConfig] No targetDate JobParameter found. Using current date: " + actualTargetDate);
        }

        // 새롭게 구현한 EtfApiPagingReader를 반환
        return new EtfApiPagingReader(etfApiService, actualTargetDate, API_PAGE_SIZE);
    }

    // 2. ItemProcessor: 읽어온 데이터를 가공 (DTO -> Entity)
    @Bean
    @StepScope
    public ItemProcessor<EtfDto, EtfEntity> etfItemProcessor() {
        return dto -> {
            EtfEntity entity = new EtfEntity();
            entity.setBasDt(dto.getBasDt());
            entity.setItmsNm(dto.getItmsNm());

            try {
                entity.setFltRt(parseBigDecimal(dto.getFltRt()));
                entity.setNav(parseBigDecimal(dto.getNav()));
                entity.setMkp(parseBigDecimal(dto.getMkp()));
                entity.setHipr(parseBigDecimal(dto.getHipr()));
                entity.setLopr(parseBigDecimal(dto.getLopr()));
                entity.setTrqu(parseLong(dto.getTrqu()));
                entity.setTrPrc(parseLong(dto.getTrPrc()));
                entity.setMrktTotAmt(parseLong(dto.getMrktTotAmt()));
                entity.setNPptTotAmt(parseLong(dto.getNPptTotAmt()));
                entity.setStLstgCnt(parseLong(dto.getStLstgCnt()));
                entity.setBssIdxClpr(parseBigDecimal(dto.getBssIdxClpr()));
                entity.setClpr(parseBigDecimal(dto.getClpr()));
                entity.setVs(parseBigDecimal(dto.getVs()));

            } catch (NumberFormatException e) {
                System.err.println("Number format error for item: " + dto.getItmsNm() + " on date " + dto.getBasDt() + ". Error: " + e.getMessage());
                return null;
            }

            entity.setSrtnCd(dto.getSrtnCd());
            entity.setIsinCd(dto.getIsinCd());
            entity.setBssIdxIdxNm(dto.getBssIdxIdxNm());

            return entity;
        };
    }

    private BigDecimal parseBigDecimal(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isEmpty())
                .map(BigDecimal::new)
                .orElse(null);
    }

    private Long parseLong(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .orElse(null);
    }

    // 3. ItemWriter: 가공된 데이터를 DB에 저장 (UPSERT)
    @Bean
    @StepScope
    public ItemWriter<EtfEntity> etfDbWriter() {
        System.out.println("[EtfBatchConfig] Initializing etfDbWriter.");
        JpaItemWriterBuilder<EtfEntity> writerBuilder = new JpaItemWriterBuilder<EtfEntity>()
                .entityManagerFactory(entityManagerFactory);
        return writerBuilder.build();
    }

    // Step 정의
    @Bean
    public Step etfDataLoadingStep() {
        System.out.println("[EtfBatchConfig] Building etfDataLoadingStep.");
        return new StepBuilder("etfDataLoadingStep", jobRepository)
                .<EtfDto, EtfEntity>chunk(5000, transactionManager) // 청크 사이즈: 5000건씩 처리
                .reader(etfApiPagingReader(null)) // @StepScope 빈은 Spring에 의해 자동 주입되므로 null 전달
                .processor(etfItemProcessor())
                .writer(etfDbWriter())
                .build();
    }

    // Job 정의
    @Bean
    public Job importEtfDataJob(EtfJobCompletionNotificationListener listener) {
        System.out.println("[EtfBatchConfig] Building importEtfDataJob.");
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(listener)
                .start(etfDataLoadingStep())
                .build();
    }
}