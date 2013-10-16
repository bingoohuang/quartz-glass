package org.n3r.quartz.glass.log.joblog.memory;

import org.n3r.quartz.glass.log.joblog.JobLog;
import org.n3r.quartz.glass.log.joblog.JobLogStore;
import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;

import java.util.ArrayList;
import java.util.List;

public class MemoryJobLogStore implements JobLogStore {
    private List<JobLog> jobLogs = new ArrayList<JobLog>();

    private static final int MAX_SIZE = 10000;

    @Override
    public synchronized Page<JobLog> getLogs(Long executionId, Query query) {
        List<JobLog> matchingJobLogs = new ArrayList<JobLog>();

        for (JobLog jobLog : jobLogs) {
            if (executionId.equals(jobLog.getExecutionId())) {
                matchingJobLogs.add(jobLog);
            }
        }

        return getLogs(matchingJobLogs, query);
    }

    @Override
    public synchronized Page<JobLog> getLogs(Query query) {
        return getLogs(jobLogs, query);
    }

    @Override
    public synchronized void add(JobLog jobLog) {
        jobLogs.add(jobLog);

        if (jobLogs.size() > MAX_SIZE) {
            jobLogs.remove(0);
        }
    }

    @Override
    public synchronized void clear() {
        jobLogs.clear();
    }

    private Page<JobLog> getLogs(List<JobLog> matchingJobLogs, Query query) {
        Page<JobLog> page = Page.fromQuery(query);

        page.setItems(query.subList(matchingJobLogs));
        page.setTotalCount(matchingJobLogs.size());

        return page;
    }
}
