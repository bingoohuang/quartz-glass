package org.n3r.quartz.glass.job;

import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.job.annotation.Job;
import org.n3r.quartz.glass.job.annotation.JobArgumentBean;
import org.n3r.quartz.glass.job.util.CurrentJobExecutionContext;
import org.n3r.quartz.glass.log.execution.CurrentJobExecution;
import org.n3r.quartz.glass.log.execution.JobExecution;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author damien bourdette
 */
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

            JobLogs.error("Exception occurred while executing job " + context.getJobDetail().getClass().getName(), exception);
        }

        executions.jobEnds(execution, context);

        JobLogs.setDefaultLevel();
        CurrentJobExecution.unset();

        CurrentJobExecutionContext.unset();
    }

    private String getLogLevel(JobExecutionContext context) {
        String level = context.getMergedJobDataMap().getString(JobArgumentBean.LOG_LEVEL_ARGUMENT);

        if (StringUtils.isNotEmpty(level)) return level;

        Job annotation = context.getJobDetail().getJobClass().getAnnotation(Job.class);

        if (annotation != null) return annotation.logLevel().name();

        return null;
    }
}
