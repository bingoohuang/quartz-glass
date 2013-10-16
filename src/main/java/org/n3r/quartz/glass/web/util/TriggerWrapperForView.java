package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.job.util.TriggerUtils;
import org.n3r.quartz.glass.util.Dates;
import org.quartz.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author damien bourdette
 */
public class TriggerWrapperForView {
    private String group;

    private String name;

    private Date startTime;

    private Date endTime;

    private String cronExpression;

    private String dataMap;

    private Trigger trigger;

    private boolean running;

    private boolean paused;

    public static List<TriggerWrapperForView> fromList(List<? extends Trigger> triggers, Scheduler scheduler) throws SchedulerException {
        List<TriggerWrapperForView> wrappers = new ArrayList<TriggerWrapperForView>();

        for (Trigger trigger : triggers) {
            wrappers.add(fromTrigger(trigger, scheduler));
        }

        return wrappers;
    }

    public static TriggerWrapperForView fromTrigger(Trigger trigger, Scheduler scheduler) throws SchedulerException {
        List<JobExecutionContext> runningJobs = scheduler.getCurrentlyExecutingJobs();

        TriggerWrapperForView wrapper = new TriggerWrapperForView();

        wrapper.trigger = trigger;
        wrapper.group = trigger.getKey().getGroup();
        wrapper.name = trigger.getKey().getName();
        wrapper.startTime = trigger.getStartTime();
        wrapper.endTime = trigger.getEndTime();
        wrapper.paused = scheduler.getTriggerState(trigger.getKey()) == Trigger.TriggerState.PAUSED;
        wrapper.dataMap = JobDataMapUtils.toProperties(trigger.getJobDataMap(), "\n");

        if (trigger instanceof CronTrigger) {
            CronTrigger cronTrigger = (CronTrigger) trigger;

            wrapper.cronExpression = cronTrigger.getCronExpression();
        }

        for (JobExecutionContext executionContext : runningJobs) {
            if (executionContext.getTrigger().equals(trigger)) {
                wrapper.running = true;

                break;
            }
        }

        return wrapper;
    }

    public String getType() {
        return (trigger instanceof CronTrigger) ? "cron" : "simple";
    }

    public String getPlanification() {
        return TriggerUtils.getPlanification(trigger);
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Date getStartTime() {
        return Dates.copy(startTime);
    }

    public Date getEndTime() {
        return Dates.copy(endTime);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getDataMap() {
        return dataMap;
    }

    public Date getPreviousFireTime() {
        return trigger.getPreviousFireTime();
    }

    public Date getNextFireTime() {
        return trigger.getNextFireTime();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }
}
