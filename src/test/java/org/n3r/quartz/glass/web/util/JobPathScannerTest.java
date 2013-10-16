package org.n3r.quartz.glass.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n3r.quartz.glass.job.demo.DummyJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/n3r/quartz/glass/spring-context.xml"})
public class JobPathScannerTest {

    @Autowired
    private JobPathScanner jobPathScanner;

    @Test
    public final void testGetJobsPaths() {
        List<String> jobs = jobPathScanner.getJobsPaths();

        Assert.assertNotNull(jobs);
        Assert.assertTrue("DummyJob must be in the list", jobs.contains(DummyJob.class.getName()));
    }

}
