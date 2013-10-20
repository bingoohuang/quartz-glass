package org.n3r.quartz.glass.util;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.quartz.Scheduler;
import org.quartz.utils.Key;

public class Keys {
    static Multiset<String> jobIndex = HashMultiset.create();

    public static String nextJobIndexPostfix(String name) {
        jobIndex.add(name);
        int count = jobIndex.count(name);
        return name + (count == 1 ? "" : ("-" + count));
    }

    public static String getFireKey() {
        return "FireNow";
    }

    public static String desc(Key<?> key) {
        return key.getGroup().equals(Scheduler.DEFAULT_GROUP) ? key.getName() : key.toString();
    }
}
