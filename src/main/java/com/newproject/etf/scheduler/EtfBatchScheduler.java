package com.newproject.etf.scheduler;

import com.newproject.etf.config.EtfBatchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EtfBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job importEtfDataJob;

    /**
     * 매일 자정 (오전 0시 0분 0초)에 Job을 실행합니다.
     * cron = "초 분 시 일 월 요일"
     * ?는 일과 요일 중 하나만 지정할 때 사용합니다.
     */
    @Scheduled(cron = "0 7 22 * * ?")
    public void runEtfDataImportJob() {
        try {
            // Job Parameters 생성
            // 'targetDate'는 배치 Job이 처리할 기준 날짜를 의미합니다.
            // 'time' 파라미터는 Job 인스턴스의 고유성을 보장하기 위해 매번 다른 값을 추가합니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매번 다른 값으로 Job 인스턴스 중복 방지
                    .toJobParameters();

            System.out.println("[EtfBatchScheduler] Launching job: " + EtfBatchConfig.JOB_NAME + " with parameters: " + jobParameters);
            // Job 실행
            jobLauncher.run(importEtfDataJob, jobParameters);
            System.out.println("[EtfBatchScheduler] Job launched successfully.");

        } catch (JobExecutionAlreadyRunningException e) {
            System.err.println("[EtfBatchScheduler] Job is already running for this instance: " + e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            System.err.println("[EtfBatchScheduler] Job instance already completed for today: " + e.getMessage());
            // 일반적으로 cron으로 매일 실행 시 JobParameters가 매번 달라지므로 이 예외는 발생하지 않습니다.
            // 하지만 JobParameters가 동일하다면 발생할 수 있습니다.
        } catch (JobRestartException e) {
            System.err.println("[EtfBatchScheduler] Job restart failed: " + e.getMessage());
        } catch (JobParametersInvalidException e) {
            System.err.println("[EtfBatchScheduler] Invalid Job Parameters: " + e.getMessage());
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            System.err.println("[EtfBatchScheduler] An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}