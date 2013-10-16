package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.job.annotation.Job;
import org.n3r.quartz.glass.job.annotation.JobArgument;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * @author damien bourdette
 */
@Job(description = "Test job for value conversions")
public class TypesJob implements org.quartz.Job {
    @JobArgument(description = "test for long value")
    private Long longValue;

    @JobArgument(description = "test for date value")
    private Date dateValue;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobLogs.info("longValue = {}", longValue);
        JobLogs.info("dateValue = {}", dateValue);
    }
}
