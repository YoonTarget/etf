package com.newproject.etf.scheduler;

import com.newproject.etf.config.EtfBatchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class EtfBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job importEtfDataJob;
    private final JobExplorer jobExplorer; // Job 상태 조회를 위해 추가

    /**
     * 매일 자정 (오전 0시 0분 0초)에 Job을 실행합니다.
     * cron = "초 분 시 일 월 요일"
     * ?는 일과 요일 중 하나만 지정할 때 사용합니다.
     */
    @Scheduled(cron = "${scheduler.etf.cron}")
    public void runEtfDataImportJob() {
        try {
            // Job Parameters 생성
            // 'targetDate'는 배치 Job이 처리할 기준 날짜를 의미합니다.
            // 재시작(Restart)을 지원하기 위해, 하루 동안은 동일한 파라미터(날짜)를 유지해야 합니다.
            String targetDate = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate)
                    .toJobParameters();

            log.info("Launching job: {} with parameters: {}", EtfBatchConfig.JOB_NAME, jobParameters);
            // Job 실행
            jobLauncher.run(importEtfDataJob, jobParameters);
            log.info("Job launched successfully.");

        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job is already running for this instance: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("Job instance already completed for today: {}", e.getMessage());
            // 일반적으로 cron으로 매일 실행 시 JobParameters가 매번 달라지므로 이 예외는 발생하지 않습니다.
            // 하지만 JobParameters가 동일하다면 발생할 수 있습니다.
        } catch (JobRestartException e) {
            log.error("Job restart failed: {}", e.getMessage());
        } catch (JobParametersInvalidException e) {
            log.error("Invalid Job Parameters: {}", e.getMessage());
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            log.error("An unexpected error occurred: {}", e.getMessage());
        }
    }

    /**
     * 실패한 Job이 있는지 주기적으로 확인하여 재시도합니다.
     * 예: 1분(60000ms)마다 실행
     */
    @Scheduled(fixedDelay = 1000 * 60)
    public void retryFailedJob() {
        try {
            String targetDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate)
                    .toJobParameters();

            // 해당 파라미터로 실행된 마지막 JobExecution 조회
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(EtfBatchConfig.JOB_NAME);
            if (lastJobInstance == null) {
                return;
            }
            JobExecution lastExecution = jobExplorer.getLastJobExecution(lastJobInstance);

            if (lastExecution != null && lastExecution.getStatus().isUnsuccessful()) {
                log.info("Found failed job for date: {}. Attempting restart...", targetDate);
                
                // 재실행 (Spring Batch는 파라미터가 같고 이전 상태가 실패면, 실패한 지점부터 이어서 실행함)
                jobLauncher.run(importEtfDataJob, jobParameters);
            }
        } catch (JobInstanceAlreadyCompleteException e) {
            // 이미 완료된 경우 무시
        } catch (JobExecutionAlreadyRunningException e) {
            // 이미 실행 중인 경우 무시
        } catch (Exception e) {
            log.error("Error during retry check: {}", e.getMessage());
        }
    }
}