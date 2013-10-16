package org.n3r.quartz.glass.configuration;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;


/**
 * @author Olivier Lagache
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/n3r/quartz/glass/spring-context.xml"})

public class VersionTest extends TestCase {

    private static final String COMPILATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private Version version;

    @Test
    public void testGetCompilationDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(COMPILATION_DATE_FORMAT);
        String compilationDateAsString = sdf.format(version.getCompilationDate());

        Assert.assertEquals("2011-09-14 21:55:41", compilationDateAsString);
    }

    @Test
    public void testGetApplicationVersion() throws Exception {
        Assert.assertEquals("0.99", version.getApplicationVersion());

    }
}
