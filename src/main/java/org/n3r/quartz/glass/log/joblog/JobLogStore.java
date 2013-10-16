package org.n3r.quartz.glass.log.joblog;

import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;

public interface JobLogStore {
    public void add(JobLog jobLog);

    public Page<JobLog> getLogs(Long executionId, Query query);

    public Page<JobLog> getLogs(Query query);

    public void clear();
}
