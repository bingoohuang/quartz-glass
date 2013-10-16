package org.n3r.quartz.glass.job.annotation;

import org.junit.Assert;
import org.junit.Test;
import org.n3r.quartz.glass.job.demo.DummyJob;

public class JobBeanTest {
    @Test
    public void getDescription() {
        Assert.assertEquals("Description is not correct", "Dummy job for testing purposes", JobBean.getDescription(DummyJob.class));
    }

    @Test
    public void isDisallowConcurrentExecution() {
        Assert.assertEquals("DummyJob do not allow concurent execution", true, JobBean.isDisallowConcurrentExecution(DummyJob.class));
    }

    @Test
    public void isPersistJobDataAfterExecution() {
        Assert.assertEquals("DummyJob do not persist data after execution", false, JobBean.isPersistJobDataAfterExecution(DummyJob.class));
    }
}
