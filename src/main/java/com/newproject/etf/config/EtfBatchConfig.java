package com.newproject.etf.config;

import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.listener.EtfJobCompletionNotificationListener;
import com.newproject.etf.service.EtfApiService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
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
public class EtfBatchConfig {

    private final EtfApiService etfApiService;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    private LocalDate targetDate; // JobParameter로 받을 날짜 저장용 필드

    public EtfBatchConfig(EtfApiService etfApiService, EntityManagerFactory entityManagerFactory,
                          PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        this.etfApiService = etfApiService;
        this.entityManagerFactory = entityManagerFactory;
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @BeforeStep // Step 실행 전에 호출되는 메서드
    public void beforeStep(StepExecution stepExecution) {
        String dateString = stepExecution.getJobParameters().getString("targetDate");
        if (dateString != null) {
            this.targetDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            System.out.println("[EtfBatchConfig] Setting targetDate from JobParameter: " + targetDate);
        } else {
            this.targetDate = LocalDate.now(); // 파라미터 없으면 기본값으로 오늘 날짜 사용
            System.out.println("[EtfBatchConfig] No targetDate JobParameter found. Using current date: " + targetDate);
        }
    }

    // 1. ItemReader: API에서 데이터를 읽어오는 부분
    @Bean
    public ItemReader<EtfDto> etfApiReader() {
        System.out.println("[EtfBatchConfig] Initializing etfApiReader for date: " + targetDate);
        List<EtfDto> allItems = etfApiService.fetchAllEtfDataForDate(targetDate)
                .collectList()
                .block(); // Flux를 List로 변환하고 블로킹

        System.out.println("[EtfBatchConfig] Finished reading data from API for date: " + targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ". Total items: " + allItems.size());
        return new ListItemReader<>(allItems);
    }

    // 2. ItemProcessor: 읽어온 데이터를 가공 (DTO -> Entity)
    @Bean
    public ItemProcessor<EtfDto, EtfEntity> etfItemProcessor() {
        return dto -> {
            EtfEntity entity = new EtfEntity();
            entity.setBasDt(dto.getBasDt());
            entity.setItmsNm(dto.getItmsNm());

            try {
                // null이거나 빈 문자열일 경우 처리하여 NumberFormatException 방지
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
                // 오류가 발생한 항목은 건너뛰기
                return null;
            }

            entity.setSrtnCd(dto.getSrtnCd());
            entity.setIsinCd(dto.getIsinCd());
            entity.setBssIdxIdxNm(dto.getBssIdxIdxNm());

            return entity;
        };
    }

    // Helper method for safe BigDecimal parsing
    private BigDecimal parseBigDecimal(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isEmpty())
                .map(BigDecimal::new)
                .orElse(null);
    }

    // Helper method for safe Long parsing
    private Long parseLong(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .orElse(null);
    }

    // 3. ItemWriter: 가공된 데이터를 DB에 저장 (UPSERT)
    @Bean
    public ItemWriter<EtfEntity> etfDbWriter() {
        System.out.println("[EtfBatchConfig] Initializing etfDbWriter.");
        JpaItemWriterBuilder<EtfEntity> writerBuilder = new JpaItemWriterBuilder<EtfEntity>()
                .entityManagerFactory(entityManagerFactory);

        // 복합 키를 사용한 UPSERT (INSERT OR UPDATE) 전략
        // 기본키가 이미 존재하면 UPDATE, 없으면 INSERT (JPA 기본 동작)
        return writerBuilder.build();
    }

    // Step 정의
    @Bean
    public Step etfDataLoadingStep() {
        System.out.println("[EtfBatchConfig] Building etfDataLoadingStep.");
        return new StepBuilder("etfDataLoadingStep", jobRepository)
                .<EtfDto, EtfEntity>chunk(100, transactionManager) // 청크 사이즈, DTO -> Entity
                .reader(etfApiReader())
                .processor(etfItemProcessor())
                .writer(etfDbWriter())
                .build();
    }

    // Job 정의
    @Bean
    public Job importEtfDataJob(EtfJobCompletionNotificationListener listener) {
        System.out.println("[EtfBatchConfig] Building importEtfDataJob.");
        return new JobBuilder("importEtfDataJob", jobRepository)
                .listener(listener) // Job 완료/실패 후 동작할 리스너
                .start(etfDataLoadingStep())
                .build();
    }
}