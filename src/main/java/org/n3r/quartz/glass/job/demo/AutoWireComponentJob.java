package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@GlassJob(description = "Test job for autowired supporting",
        team = "火箭队", created = "2014-09-02")
@Component
public class AutoWireComponentJob implements Job {
    @Autowired
    private Configuration configuration;

    public void execute(JobExecutionContext context) {
        JobLogs.info("AutoWireComponentJob {}, configuration :{}", this, configuration);
    }
}

