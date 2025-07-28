package com.newproject.etf.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// Job이 완료된 후 실행될 리스너
@Component
public class EtfJobCompletionNotificationListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EtfJobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("[JobListener] Job starting: " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("!!! JOB FINISHED! Time to verify the results");

            // Job 완료 후 DB에 저장된 데이터 확인 (샘플 쿼리)
            try {
                // EtfEntity의 테이블명과 컬럼명에 맞춰 쿼리 수정
                jdbcTemplate.query("SELECT bas_dt, itms_nm, clpr, trqu FROM etf_price_info LIMIT 10",
                                (rs, row) -> "Date: " + rs.getString("bas_dt") +
                                        ", Item: " + rs.getString("itms_nm") +
                                        ", Closing Price: " + rs.getBigDecimal("clpr") +
                                        ", Volume: " + rs.getLong("trqu"))
                        .forEach(System.out::println);
            } catch (Exception e) {
                System.err.println("Error querying DB after job: " + e.getMessage());
            }

        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            System.err.println("!!! JOB FAILED! Check the logs for errors.");
            jobExecution.getAllFailureExceptions().forEach(e -> System.err.println(e.getMessage()));
        }
    }
}