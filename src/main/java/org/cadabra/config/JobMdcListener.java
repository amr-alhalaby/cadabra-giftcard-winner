package org.cadabra.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobMdcListener implements JobExecutionListener {

    private static final String JOB_EXECUTION_ID_KEY = "jobExecutionId";

    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        MDC.put(JOB_EXECUTION_ID_KEY, String.valueOf(jobExecution.getId()));
        log.info("Job [{}] started — executionId: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        log.info("Job [{}] finished — executionId: {}, status: {}",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId(), jobExecution.getStatus());
        MDC.remove(JOB_EXECUTION_ID_KEY);
    }
}

