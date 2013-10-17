package org.n3r.quartz.glass.job;

import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.JobArgumentBean;
import org.n3r.quartz.glass.job.util.CurrentJobExecutionContext;
import org.n3r.quartz.glass.log.execution.CurrentJobExecution;
import org.n3r.quartz.glass.log.execution.JobExecution;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.n3r.quartz.glass.util.Jobs;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlassJobListener extends JobListenerSupport {
    @Autowired
    private JobExecutions executions;

    @Override
    public String getName() {
        return GlassJobListener.class.getName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        CurrentJobExecutionContext.set(context);

        JobExecution execution = executions.jobStarts(context);

        CurrentJobExecution.set(execution);
        JobLogs.setLevel(getLogLevel(context));
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
        JobExecution execution = CurrentJobExecution.get();

        if (exception != null) {
            execution.error();

            JobDetail jobDetail = context.getJobDetail();
            JobLogs.error("Exception occurred while executing job "
                    + Jobs.jobCass(jobDetail).getName(), exception);
        }

        executions.jobEnds(execution, context);

        JobLogs.setDefaultLevel();
        CurrentJobExecution.unset();

        CurrentJobExecutionContext.unset();
    }



    private String getLogLevel(JobExecutionContext context) {
        String level = context.getMergedJobDataMap().getString(JobArgumentBean.LOG_LEVEL_ARGUMENT);
        if (StringUtils.isNotEmpty(level)) return level;

        GlassJob annotation = Jobs.glassJob(context.getJobDetail());

        if (annotation != null) return annotation.logLevel().name();

        return null;
    }
}
