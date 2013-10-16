package org.n3r.quartz.glass.log.execution;

import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;
import org.quartz.JobExecutionContext;

/**
 * JobExecutions service to store JobExecution objects
 *
 * @author damien bourdette
 */
public interface JobExecutions {
    public JobExecution jobStarts(JobExecutionContext context);

    public void jobEnds(JobExecution execution, JobExecutionContext context);

    public Page<JobExecution> find(Query query);

    public Page<JobExecution> find(String jobGroup, String jobName, Query query);

    public void clear();
}
