package com.newproject.etf.config;

import com.newproject.etf.batch.EtfApiPagingReader; // 새롭게 만든 Reader 임포트
import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.listener.EtfJobCompletionNotificationListener;
import com.newproject.etf.service.EtfApiService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class EtfBatchConfig {

    public static final String JOB_NAME = "importEtfDataJob";
    private static final int API_PAGE_SIZE = 10000; // API에서 한 번에 가져올 최대 건수

    private final EtfApiService etfApiService;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final DataSource dataSource; // JDBC Writer 사용을 위해 주입


    // 1. ItemReader: API에서 데이터를 페이지별로 읽어오는 부분
    @Bean
    @StepScope // Step 실행 시점에 생성되도록 StepScope 사용
    public EtfApiPagingReader etfApiPagingReader() {
        // 새롭게 구현한 EtfApiPagingReader를 반환
        return new EtfApiPagingReader(etfApiService, API_PAGE_SIZE);
    }

    // 2. Processor: 중복 체크 + 데이터 변환
    @Bean
    @StepScope
    public ItemProcessor<EtfDto, EtfEntity> etfItemProcessor() {
        return dto -> {
            // 메모리 기반 중복 체크 로직 제거 (DB에서 처리)

            EtfEntity entity = new EtfEntity();
            entity.setSrtnCd(dto.getSrtnCd());
            entity.setBasDt(dto.getBasDt());

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
                log.error("Number format error for item: {} on date {}", dto.getItmsNm(), dto.getBasDt());
                return null; // 변환 불가 항목 스킵
            }

            entity.setItmsNm(dto.getItmsNm());
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
        log.info("Initializing etfDbWriter.");
        // JpaItemWriter 대신 JdbcBatchItemWriter 사용 (INSERT IGNORE / ON CONFLICT 지원)
        return new JdbcBatchItemWriterBuilder<EtfEntity>()
                .dataSource(dataSource)
                .sql("INSERT INTO etf_price_info (" +
                        "srtn_cd, bas_dt, flt_rt, nav, mkp, hipr, lopr, trqu, tr_prc, " +
                        "mrkt_tot_amt, n_ppt_tot_amt, st_lstg_cnt, bss_idx_clpr, clpr, vs, " +
                        "itms_nm, isin_cd, bss_idx_idx_nm) " +
                        "VALUES (" +
                        ":srtnCd, :basDt, :fltRt, :nav, :mkp, :hipr, :lopr, :trqu, :trPrc, " +
                        ":mrktTotAmt, :nPptTotAmt, :stLstgCnt, :bssIdxClpr, :clpr, :vs, " +
                        ":itmsNm, :isinCd, :bssIdxIdxNm) " +
                        "ON CONFLICT (srtn_cd, bas_dt) DO NOTHING")
                .beanMapped()
                .assertUpdates(false) // 중복 데이터(DO NOTHING) 발생 시 0건 업데이트되어도 오류로 처리하지 않음
                .build();
    }

    // Step 정의
    @Bean
    public Step etfDataLoadingStep() {
        log.info("Building etfDataLoadingStep.");
        return new StepBuilder("etfDataLoadingStep", jobRepository)
                .<EtfDto, EtfEntity>chunk(50, transactionManager) // 청크 사이즈를 더 축소 (100 -> 50)
                .reader(etfApiPagingReader())
                .processor(etfItemProcessor())
                .writer(etfDbWriter())
                .transactionManager(transactionManager)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        // 각 페이지 처리 전 5초 대기
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .build();
    }

    // Job 정의
    @Bean
    public Job importEtfDataJob(EtfJobCompletionNotificationListener listener) {
        log.info("Building importEtfDataJob.");
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(listener)
                .start(etfDataLoadingStep())
                .build();
    }
}