package com.newproject.etf.listener;

import com.newproject.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId; // ZoneId 임포트
import java.time.ZonedDateTime; // ZonedDateTime 임포트
import java.time.Duration; // Duration 임포트

@Component
@Slf4j
@RequiredArgsConstructor
public class EtfJobCompletionNotificationListener implements JobExecutionListener {

    private EtfService eftService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("📢📢📢 ETF Data Import Job이 시작됩니다! Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("Start Time: {}", jobExecution.getStartTime());
        log.info("Job Parameters: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("🎉🎉🎉 ETF Data Import Job이 성공적으로 완료되었습니다! 🎉🎉🎉");
            log.info("End Time: {}", jobExecution.getEndTime());

            eftService.invalidateEtfCache();

            // 시작 시간과 종료 시간 모두 LocalDateTime이므로 Duration을 사용하여 시간 차이를 계산
            if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
                Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
                log.info("Total Execution Time: {} ms", duration.toMillis()); // 밀리초 단위
                log.info("Total Execution Time: {} seconds", duration.getSeconds()); // 초 단위
            } else {
                log.warn("Job start or end time is null, cannot calculate total execution time.");
            }

            log.info("Processed Items: {}", jobExecution.getStepExecutions().stream()
                    .mapToLong(stepExecution -> stepExecution.getWriteCount())
                    .sum());
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("🔥🔥🔥 ETF Data Import Job이 실패했습니다! 🔥🔥🔥");
            log.error("End Time: {}", jobExecution.getEndTime());
            log.error("Exit Status: {}", jobExecution.getExitStatus().getExitCode());
            log.error("Failure Cause: {}", jobExecution.getFailureExceptions());
        } else {
            log.warn("⚠️⚠️⚠️ ETF Data Import Job이 예상치 못한 상태로 종료되었습니다! Status: {}", jobExecution.getStatus());
        }
    }
}