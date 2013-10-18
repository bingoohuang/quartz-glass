package org.n3r.quartz.glass.web.controller;

import org.joda.time.DateTime;
import org.n3r.quartz.glass.configuration.Configuration;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * The home page !
 */
@Controller
public class IndexController {

    @Autowired
    protected Scheduler quartzScheduler;

    @Autowired
    protected Configuration configuration;

    @RequestMapping({"/", "/index"})
    public String dashboard(Model model) throws SchedulerException {
        List<JobExecutionContext> runningJobs = quartzScheduler.getCurrentlyExecutingJobs();

        List<Trigger> pausedTriggers = new ArrayList<Trigger>();
        List<Trigger> hangedTriggers = new ArrayList<Trigger>();

        List<String> groups = quartzScheduler.getJobGroupNames();

        Collections.sort(groups);

        for (String group : groups) {
            Set<JobKey> jobKeys = quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group));

            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = quartzScheduler.getTriggersOfJob(jobKey);

                for (Trigger trigger : triggers) {
                    if (isPaused(trigger)) {
                        pausedTriggers.add(trigger);
                    } else if (isHanged(trigger, runningJobs)) {
                        hangedTriggers.add(trigger);
                    }
                }
            }
        }

        model.addAttribute("runningJobs", runningJobs);
        model.addAttribute("pausedTriggers", pausedTriggers);
        model.addAttribute("hangedTriggers", hangedTriggers);

        return "dashboard";
    }

    @RequestMapping("/start")
    public String start() throws SchedulerException {
        quartzScheduler.start();

        return "redirect:/glass/";
    }

    @RequestMapping("/standby")
    public String standby() throws SchedulerException {
        quartzScheduler.standby();

        return "redirect:/glass/";
    }

    @RequestMapping("/restartTrigger")
    public String restartTrigger(String group, String name) throws SchedulerException {
        Trigger trigger = quartzScheduler.getTrigger(new TriggerKey(name, group));

        if (trigger == null) return "redirect:/glass/";

        trigger = trigger.getTriggerBuilder().startAt(new Date()).build();

        quartzScheduler.rescheduleJob(trigger.getKey(), trigger);

        return "redirect:/glass/";
    }

    @RequestMapping("/interrupt")
    public String interrupt(String group, String name) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/";

        quartzScheduler.interrupt(job.getKey());

        return "redirect:/glass/";
    }

    private boolean isPaused(Trigger trigger) throws SchedulerException {
        return quartzScheduler.getTriggerState(trigger.getKey()) == TriggerState.PAUSED;
    }

    private boolean isHanged(Trigger trigger, List<JobExecutionContext> runningJobs) throws SchedulerException {
        Date nextFireTime = trigger.getNextFireTime();

        if (nextFireTime == null) return false;

        if (isRunning(trigger, runningJobs)) return false;

        Date oneMinuteAgo = new DateTime().minusMinutes(1).toDate();

        return nextFireTime.before(oneMinuteAgo);
    }

    private boolean isRunning(Trigger trigger, List<JobExecutionContext> runningJobs) {
        for (JobExecutionContext runningJob : runningJobs) {
            if (runningJob.getTrigger().getKey().equals(trigger.getKey())) {
                return true;
            }
        }

        return false;
    }

}
