package org.n3r.quartz.glass.util;

import org.quartz.Scheduler;
import org.quartz.utils.Key;

public class Keys {
    public static String desc(Key<?> key) {
        return key.getGroup().equals(Scheduler.DEFAULT_GROUP) ? key.getName() : key.toString();
    }
}
