package org.n3r.quartz.glass.diamond;

import org.quartz.Scheduler;

public class TriggerBean {
    private String name = "";
    private String group = Scheduler.DEFAULT_GROUP;
    private String triggerDataMap;
    private String scheduler;
    private long startDelay;
    private String jobClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTriggerDataMap() {
        return triggerDataMap;
    }

    public void setTriggerDataMap(String triggerDataMap) {
        this.triggerDataMap = triggerDataMap;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }
}
