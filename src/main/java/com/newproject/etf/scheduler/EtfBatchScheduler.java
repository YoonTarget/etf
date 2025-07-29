// src/main/java/com/newproject/etf/scheduler/EtfBatchScheduler.java (예시)
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
    private final Job importEtfDataJob; // EtfBatchConfig에서 정의한 Job 빈 주입

    // 매일 새벽 2시에 실행 (예시)
    // 초 분 시 일 월 요일
    // @Scheduled(cron = "0 0 2 * * ?") // 실제 운영 시 사용
    @Scheduled(fixedRate = 10000) // 테스트를 위해 10초마다 실행 (배포 시 주석 처리)
    public void runEtfDataImportJob() {
        try {
            // Job Parameters 생성
            // 'targetDate' 파라미터는 'YYYYMMDD' 형식으로 전달
            // Job 인스턴스의 고유성을 보장하기 위해 date와 timestamp를 함께 사용
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", currentDate) // 이 파라미터가 @Value("#{jobParameters[targetDate]}")로 주입됩니다.
                    .addLong("time", System.currentTimeMillis()) // Job 인스턴스 중복 방지 (필수)
                    .toJobParameters();

            System.out.println("[EtfBatchScheduler] Launching job: " + EtfBatchConfig.JOB_NAME + " with targetDate: " + currentDate);
            jobLauncher.run(importEtfDataJob, jobParameters);
            System.out.println("[EtfBatchScheduler] Job launched successfully.");

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            System.err.println("[EtfBatchScheduler] Error running job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}