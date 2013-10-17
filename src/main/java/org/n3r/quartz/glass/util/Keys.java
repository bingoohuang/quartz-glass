package org.n3r.quartz.glass.util;

import org.quartz.Scheduler;
import org.quartz.utils.Key;

import java.util.concurrent.atomic.AtomicLong;

public class Keys {
    static AtomicLong jobIndex = new AtomicLong(0);
    static AtomicLong fireIndex = new AtomicLong(0);

    public static String nextJobIndexPostfix() {
        return "-" + jobIndex.incrementAndGet();
    }

    public static String getFireKey() {
        return "FireNow-" + fireIndex.incrementAndGet();
    }
    public static String desc(Key<?> key) {
        return key.getGroup().equals(Scheduler.DEFAULT_GROUP) ? key.getName() : key.toString();
    }
}
