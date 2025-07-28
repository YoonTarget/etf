package com.newproject.etf.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@EnableScheduling // 스케줄링 기능 활성화
public class EtfBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job importEtfDataJob;

    public EtfBatchScheduler(JobLauncher jobLauncher, Job importEtfDataJob) {
        this.jobLauncher = jobLauncher;
        this.importEtfDataJob = importEtfDataJob;
    }

    // 개발/테스트 목적으로 애플리케이션 시작 후 10초 뒤에 Job을 한 번 실행합니다.
    // @PostConstruct
    // public void runJobOnStartup() {
    //     System.out.println("[Scheduler] Running ETF Import Job on startup for testing...");
    //     // 특정 날짜를 지정하여 테스트 (예: 2024년 6월 1일)
    //     runEtfImportJob(LocalDate.of(2024, 6, 1)); // 실제 API 호출 시 해당 날짜에 데이터가 있는지 확인
    //     // runEtfImportJob(LocalDate.now()); // 또는 오늘 날짜
    // }

    // 매일 새벽 3시에 실행되도록 설정 (실제 운영 시 사용)
    // 현재 시간은 2025년 7월 28일 8시 42분 KST 이므로,
    // 테스트를 위해 잠시 크론 표현식을 현재 시간 근처로 조정하거나,
    // 위 @PostConstruct 주석을 해제하여 수동으로 실행해 볼 수 있습니다.
    // 예: 매분 0초에 실행 (테스트용): cron = "0 * * * * ?"
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 0분 0초
    public void runDailyEtfImportJob() {
        System.out.println("[Scheduler] Scheduled daily ETF Import Job started.");
        // JobParameters에 targetDate를 추가하여 BatchConfig의 ItemReader에 전달합니다.
        runEtfImportJob(LocalDate.now()); // 매일 실행 시 현재 날짜 전달
    }

    // Job 실행을 위한 공통 메서드
    private void runEtfImportJob(LocalDate targetDate) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", String.valueOf(System.currentTimeMillis())) // Job Instance를 고유하게 만들기 위함
                .addString("targetDate", targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))) // JobParameter로 날짜 전달
                .addString("runTime", LocalDateTime.now().toString()) // 추가적인 고유성 확보
                .toJobParameters();
        try {
            jobLauncher.run(importEtfDataJob, jobParameters);
            System.out.println("[Scheduler] ETF Import Job Started Successfully for date: " + targetDate);
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            System.err.println("[Scheduler] Error running ETF Import Job for date " + targetDate + ": " + e.getMessage());
        }
    }
}