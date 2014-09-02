package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.GlassTrigger;
import org.n3r.quartz.glass.job.annotation.JobArgument;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@GlassTrigger(scheduler = "Every 3 seconds", triggerDataMap = "name=冰果皇")
@GlassJob(description = "Test job for autowired supporting",
        team = "火箭队", created = "2014-09-02")
public class AutoWireComponentJob implements Job {
    @Autowired
    private Configuration configuration;
    @JobArgument(description = "姓名", sampleValues = {"张三", "李四"})
    private String name;

    public void execute(JobExecutionContext context) {
        JobLogs.info("AutoWireComponentJob {}, configuration :{}, name:{}", this, configuration, name);
    }
}

