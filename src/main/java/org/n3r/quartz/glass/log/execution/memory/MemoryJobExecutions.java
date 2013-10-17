package org.n3r.quartz.glass.log.execution.memory;

import org.joda.time.DateTime;
import org.n3r.quartz.glass.log.execution.JobExecution;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryJobExecutions implements JobExecutions {
    private final List<JobExecution> executions = new ArrayList<JobExecution>();

    private static final int MAX_SIZE = 1000;

    private static Long identifier = 0l;

    @Override
    public synchronized JobExecution jobStarts(JobExecutionContext context) {
        identifier++;

        JobExecution execution = new JobExecution();

        execution.setId(identifier);
        execution.fillWithContext(context);

        addLog(execution);

        return execution;
    }

    @Override
    public synchronized void jobEnds(JobExecution execution, JobExecutionContext context) {
        execution.setEndDate(new DateTime(context.getFireTime())
                .plusMillis((int) context.getJobRunTime()).toDate());
        execution.setEnded(true);
    }

    @Override
    public synchronized Page<JobExecution> find(Query query) {
        if (query.getResult() != null) {
            List<JobExecution> matchingLogs = new ArrayList<JobExecution>();

            for (JobExecution execution : executions) {
                if (query.getResult() == execution.getResult()) {
                    matchingLogs.add(execution);
                }
            }

            return getLogs(matchingLogs, query);
        } else {
            return getLogs(executions, query);
        }
    }

    @Override
    public synchronized Page<JobExecution> find(String jobGroup, String jobName, Query query) {
        List<JobExecution> matchingLogs = new ArrayList<JobExecution>();

        for (JobExecution execution : executions) {
            if (jobGroup.equals(execution.getJobGroup()) && jobName.equals(execution.getJobName())) {
                matchingLogs.add(execution);
            }
        }

        return getLogs(matchingLogs, query);
    }

    @Override
    public synchronized void clear() {
        executions.clear();
    }

    private void addLog(JobExecution execution) {
        executions.add(execution);

        if (executions.size() > MAX_SIZE) {
            executions.remove(0);
        }
    }

    private Page<JobExecution> getLogs(List<JobExecution> matchingExecutions, Query query) {
        Page<JobExecution> page = Page.fromQuery(query);

        List<JobExecution> items = new ArrayList<JobExecution>();
        items.addAll(matchingExecutions);

        Collections.reverse(items);
        List<JobExecution> subList = query.subList(items);

        page.setItems(subList);
        page.setTotalCount(matchingExecutions.size());

        return page;
    }
}
