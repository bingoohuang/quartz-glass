package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.JobArgument;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

@GlassJob(description = "Test job for value conversions",
        team = "火箭队", created = "2013-10-16")
public class TypesJob implements Job {
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
