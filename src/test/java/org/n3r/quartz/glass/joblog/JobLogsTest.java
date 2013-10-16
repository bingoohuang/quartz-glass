package org.n3r.quartz.glass.joblog;

import org.junit.Before;
import org.junit.Test;
import org.n3r.quartz.glass.log.joblog.JobLogLevel;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.n3r.quartz.glass.log.joblog.memory.MemoryJobLogStore;
import org.n3r.quartz.glass.util.Query;

import static junit.framework.Assert.assertEquals;

public class JobLogsTest {
    private MemoryJobLogStore store = new MemoryJobLogStore();

    @Before
    public void init() {
        store.clear();

        JobLogs.jobLogStore = store;
    }

    @Test
    public void info() {
        JobLogs.setLevel(JobLogLevel.INFO);

        JobLogs.debug("this should not go in logs");
        assertEquals(0, store.getLogs(Query.firstPage()).getTotalCount());

        JobLogs.info("this one should do");
        assertEquals(1, store.getLogs(Query.firstPage()).getTotalCount());
    }
}
