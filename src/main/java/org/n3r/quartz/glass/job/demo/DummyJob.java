package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.JobArgument;
import org.n3r.quartz.glass.log.joblog.JobLogLevel;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.*;

/**
 * A dummy quartz job for testing purposes.
 */
@GlassJob(description = "演示JOB", logLevel = JobLogLevel.INFO,
    team= "火箭队", created = "2013-10-16")
@DisallowConcurrentExecution
public class DummyJob implements InterruptableJob {

    @JobArgument(required = true, description = "Duration of the job, in seconds. Default is 10 seconds", sampleValues = "10, 60")
    private Long duration = 10l;

    private Thread runningThread;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        runningThread = Thread.currentThread();

        if (duration < 2) {
            JobLogs.error("Running dummy job for {} seconds", duration);
        } else if (duration < 4) {
            JobLogs.warn("Running dummy job for {} seconds", duration);
        } else {
            JobLogs.info("Running dummy job for {} seconds", duration);
        }

        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        runningThread.interrupt();
    }
}
