package com.newproject.etf.service;

import com.newproject.etf.config.EtfBatchConfig;
import com.newproject.etf.repository.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatchMetadataService {

    private static final DateTimeFormatter BAS_DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter BATCH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EtfRepository etfRepository;
    private final JobExplorer jobExplorer;

    public String getLatestBasDtLabel() {
        String latestBasDt = etfRepository.findMaxBasDt();
        if (latestBasDt == null || latestBasDt.length() != 8) {
            return null;
        }

        return LocalDateTime.parse(latestBasDt + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                .toLocalDate()
                .format(BAS_DT_FORMATTER);
    }

    public String getLastSuccessfulBatchAtLabel() {
        return findLastSuccessfulBatchTime()
                .map(time -> time.format(BATCH_TIME_FORMATTER))
                .orElse(null);
    }

    private Optional<LocalDateTime> findLastSuccessfulBatchTime() {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(EtfBatchConfig.JOB_NAME, 0, 20);

        for (JobInstance jobInstance : jobInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
            for (JobExecution execution : executions) {
                if (execution.getStatus() == BatchStatus.COMPLETED && execution.getEndTime() != null) {
                    return Optional.of(execution.getEndTime());
                }
            }
        }

        return Optional.empty();
    }
}
