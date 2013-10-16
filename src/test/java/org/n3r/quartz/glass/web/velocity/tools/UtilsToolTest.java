package org.n3r.quartz.glass.web.velocity.tools;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.n3r.quartz.glass.job.demo.DummyJob;
import org.n3r.quartz.glass.tools.UtilsTool;
import org.quartz.impl.JobDetailImpl;

public class UtilsToolTest {

    private UtilsTool utilsTool = new UtilsTool();

    @Test
    public void testIsInterruptable() throws Exception {
        JobDetailImpl job = new JobDetailImpl();
        job.setJobClass(DummyJob.class);

        Assert.assertEquals(true, utilsTool.isInterruptible(job));
    }

    @Test
    public void duration() throws Exception {
        DateTime start = new DateTime();

        Assert.assertEquals("6s", utilsTool.duration(start.toDate(), start.plusSeconds(6).toDate()));
        Assert.assertEquals("1m 2s", utilsTool.duration(start.toDate(), start.plusSeconds(62).toDate()));
        Assert.assertEquals("2h 2s", utilsTool.duration(start.toDate(), start.plusHours(2).plusSeconds(2).toDate()));
    }
}
