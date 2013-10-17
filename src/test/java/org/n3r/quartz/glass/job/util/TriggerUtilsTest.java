package org.n3r.quartz.glass.job.util;

import org.junit.Assert;
import org.junit.Test;

public class TriggerUtilsTest {
    @Test
    public void getPlanification() {
        Assert.assertEquals("repeat 10 times every 100 millis", TriggerUtils.getPlanification(10, 100));
        Assert.assertEquals("repeat forever every 100 millis", TriggerUtils.getPlanification(-1, 100));
        Assert.assertEquals("execute once", TriggerUtils.getPlanification(0, 100));
        Assert.assertEquals("repeat one time in 100 millis", TriggerUtils.getPlanification(1, 100));
    }
}
