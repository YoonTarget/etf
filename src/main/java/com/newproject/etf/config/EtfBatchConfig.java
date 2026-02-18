package com.newproject.etf.config;

import com.newproject.etf.batch.EtfApiPagingReader;
import com.newproject.etf.dto.EtfDto;
import com.newproject.etf.entity.EtfEntity;
import com.newproject.etf.listener.EtfJobCompletionNotificationListener;
import com.newproject.etf.repository.EtfRepository;
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
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class EtfBatchConfig {

    public static final String JOB_NAME = "importEtfDataJob";
    
    private static final int API_PAGE_SIZE = 10000; 
    private static final int CHUNK_SIZE = 1000;

    private final EtfApiService etfApiService;
    private final EtfRepository etfRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final DataSource dataSource;

    @Bean
    @StepScope
    public EtfApiPagingReader etfApiPagingReader() {
        return new EtfApiPagingReader(etfApiService, etfRepository, API_PAGE_SIZE);
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
                return null;
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
    public ItemWriter<EtfEntity> etfPriceDbWriter() {
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
    @StepScope
    public ItemWriter<EtfEntity> etfInfoDbWriter() {
        return new JdbcBatchItemWriterBuilder<EtfEntity>()
                .dataSource(dataSource)
                .sql("INSERT INTO etf_info (srtn_cd, itms_nm, isin_cd) " +
                     "VALUES (:srtnCd, :itmsNm, :isinCd) " +
                     "ON CONFLICT (srtn_cd) DO NOTHING")
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    @StepScope
    public CompositeItemWriter<EtfEntity> compositeItemWriter() {
        CompositeItemWriter<EtfEntity> writer = new CompositeItemWriter<>();
        writer.setDelegates(Arrays.asList(etfPriceDbWriter(), etfInfoDbWriter()));
        return writer;
    }

    @Bean
    public Step etfDataLoadingStep() {
        log.info("Building etfDataLoadingStep.");
        return new StepBuilder("etfDataLoadingStep", jobRepository)
                .<EtfDto, EtfEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(etfApiPagingReader())
                .processor(etfItemProcessor())
                .writer(compositeItemWriter()) // Composite Writer 사용
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
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