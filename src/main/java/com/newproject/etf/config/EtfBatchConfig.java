package com.newproject.etf.config;

import com.newproject.etf.batch.EtfApiPagingReader;
import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.listener.EtfJobCompletionNotificationListener;
import com.newproject.etf.service.EtfApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class EtfBatchConfig {

    public static final String JOB_NAME = "importEtfDataJob";
    
    // [Efficiency] API 호출 횟수 최소화를 위해 한 번에 많이 가져옴 (API 제한 고려)
    private static final int API_PAGE_SIZE = 10000; 
    
    // [Performance] DB 트랜잭션 효율을 위해 적절한 크기로 끊어서 커밋
    private static final int CHUNK_SIZE = 1000;

    private final EtfApiService etfApiService;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final DataSource dataSource;

    @Bean
    @StepScope
    public EtfApiPagingReader etfApiPagingReader() {
        return new EtfApiPagingReader(etfApiService, API_PAGE_SIZE);
    }

    @Bean
    @StepScope
    public ItemProcessor<EtfDto, EtfEntity> etfItemProcessor() {
        return dto -> {
            EtfEntity entity = new EtfEntity();
            entity.setSrtnCd(dto.getSrtnCd());
            entity.setBasDt(dto.getBasDt());
            entity.setItmsNm(dto.getItmsNm());
            entity.setIsinCd(dto.getIsinCd());
            entity.setBssIdxIdxNm(dto.getBssIdxIdxNm());

            try {
                // [Data Integrity] Nullable 필드에 대한 안전한 파싱 및 기본값 처리
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
                log.warn("Number format error for item: {} on date {}. Skipping item.", dto.getItmsNm(), dto.getBasDt());
                return null; // 파싱 에러 시 해당 항목 스킵 (Fault Tolerance)
            }
            return entity;
        };
    }

    private BigDecimal parseBigDecimal(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.trim().isEmpty())
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    private Long parseLong(String value) {
        return Optional.ofNullable(value)
                .filter(s -> !s.trim().isEmpty())
                .map(Long::parseLong)
                .orElse(0L);
    }

    @Bean
    @StepScope
    public ItemWriter<EtfEntity> etfDbWriter() {
        log.info("Initializing etfDbWriter.");
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
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step etfDataLoadingStep() {
        log.info("Building etfDataLoadingStep.");
        return new StepBuilder("etfDataLoadingStep", jobRepository)
                .<EtfDto, EtfEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(etfApiPagingReader())
                .processor(etfItemProcessor())
                .writer(etfDbWriter())
                // [Resilience] 내결함성 설정: 예외 발생 시 즉시 실패하지 않고 스킵
                .faultTolerant()
                .skip(Exception.class) // 모든 예외에 대해 스킵 시도 (운영 정책에 따라 구체화 가능)
                .skipLimit(100)        // 최대 100개까지 에러 허용
                .build();
    }

    @Bean
    public Job importEtfDataJob(EtfJobCompletionNotificationListener listener) {
        log.info("Building importEtfDataJob.");
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(listener)
                .start(etfDataLoadingStep())
                .build();
    }
}