package org.n3r.quartz.glass.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.job.annotation.JobArgumentBean;
import org.n3r.quartz.glass.web.form.*;
import org.n3r.quartz.glass.web.util.JobAndTriggers;
import org.n3r.quartz.glass.web.util.TriggerWrapperForView;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

@Controller
public class TriggersController {
    @Autowired
    protected Scheduler quartzScheduler;

    @Autowired
    protected Configuration configuration;

    @RequestMapping("/triggers")
    public String all(Model model) throws SchedulerException {
        List<JobAndTriggers> jobsAndTriggers = new ArrayList<JobAndTriggers>();

        for (String group : quartzScheduler.getJobGroupNames()) {
            GroupMatcher<JobKey> groupMatcher = groupEquals(group);
            for (JobKey jobKey : quartzScheduler.getJobKeys(groupMatcher)) {
                JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);

                JobAndTriggers jobAndTrigger = new JobAndTriggers();
                jobAndTrigger.setJobDetail(jobDetail);
                jobAndTrigger.setTriggers(TriggerWrapperForView.fromList(quartzScheduler.getTriggersOfJob(jobKey), quartzScheduler));

                jobsAndTriggers.add(jobAndTrigger);
            }
        }

        model.addAttribute("jobsAndTriggers", jobsAndTriggers);

        return "triggers";
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/new-cron")
    public String createCronTrigger(@PathVariable String group, @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        model.addAttribute("form", new NewCronTriggerForm(job));
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

        return "new_cron_trigger_form";
    }

    @RequestMapping(value = "/jobs/{group}/{name}/triggers/new-cron", method = RequestMethod.POST)
    public String postCreateCronTrigger(@PathVariable String group, @PathVariable String name,
                                        @Valid @ModelAttribute("form") NewCronTriggerForm form, BindingResult result, Model model) throws SchedulerException, ParseException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        if (result.hasErrors()) {
            model.addAttribute("form", form);
            model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

            return "new_cron_trigger_form";
        }

        quartzScheduler.scheduleJob(form.getTrigger());

        return "redirect:/glass/jobs/{group}/{name}";
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/new-simple")
    public String createSimpleTrigger(@PathVariable String group, @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        model.addAttribute("form", new NewSimpleTriggerForm(job));
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

        return "new_simple_trigger_form";
    }

    @RequestMapping(value = "/jobs/{group}/{name}/triggers/new-simple", method = RequestMethod.POST)
    public String postCreateSimpleTrigger(@PathVariable String group, @PathVariable String name, @Valid @ModelAttribute("form") NewSimpleTriggerForm form, BindingResult result, Model model) throws SchedulerException, ParseException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        if (result.hasErrors()) {
            model.addAttribute("form", form);
            model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

            return "new_simple_trigger_form";
        }

        quartzScheduler.scheduleJob(form.getTrigger());

        return "redirect:/glass/jobs/{group}/{name}";
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/edit")
    public String edit(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        Trigger trigger = quartzScheduler.getTrigger(new TriggerKey(triggerName, triggerGroup));

        if (trigger == null) return "redirect:/glass/jobs/{group}/{name}";

        model.addAttribute("trigger", trigger);
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

        if (trigger instanceof CronTrigger) {
            model.addAttribute("form", new CronTriggerForm(trigger));

            return "cron_trigger_form";
        } else {
            model.addAttribute("form", new SimpleTriggerForm(trigger));

            return "simple_trigger_form";
        }
    }

    @RequestMapping(value = "/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/edit-cron", method = RequestMethod.POST)
    public String postEditCronTrigger(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, @Valid @ModelAttribute("form") CronTriggerForm form, BindingResult result, Model model) throws SchedulerException, ParseException {
        return postEditTrigger(group, name, triggerGroup, triggerName, form, model, result);
    }

    @RequestMapping(value = "/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/edit-simple", method = RequestMethod.POST)
    public String postEditSimpleTrigger(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, @Valid @ModelAttribute("form") SimpleTriggerForm form, BindingResult result, Model model) throws SchedulerException, ParseException {
        return postEditTrigger(group, name, triggerGroup, triggerName, form, model, result);
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/delete")
    public String delete(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, @RequestParam(required = false) String redirect) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        quartzScheduler.unscheduleJob(new TriggerKey(triggerName, triggerGroup));

        if (StringUtils.isNotEmpty(redirect)) return "redirect:" + redirect;

        return "redirect:/glass";
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/pause")
    public String pause(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, @RequestParam(required = false) String redirect) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        quartzScheduler.pauseTrigger(new TriggerKey(triggerName, triggerGroup));

        if (StringUtils.isNotEmpty(redirect)) return "redirect:" + redirect;

        return "redirect:/glass";
    }

    @RequestMapping("/jobs/{group}/{name}/triggers/{triggerGroup}/{triggerName}/resume")
    public String resume(@PathVariable String group, @PathVariable String name, @PathVariable String triggerGroup, @PathVariable String triggerName, @RequestParam(required = false) String redirect) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        quartzScheduler.resumeTrigger(new TriggerKey(triggerName, triggerGroup));

        if (StringUtils.isNotEmpty(redirect)) return "redirect:" + redirect;

        return "redirect:/glass";
    }

    private String postEditTrigger(String group, String name, String triggerGroup, String triggerName, TriggerForm form, Model model, BindingResult result) throws SchedulerException, ParseException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        Trigger trigger = quartzScheduler.getTrigger(new TriggerKey(triggerName, triggerGroup));

        if (trigger == null) return "redirect:/glass/jobs/{group}/{name}";

        if (result.hasErrors()) {
            model.addAttribute("trigger", trigger);
            model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));

            if (trigger instanceof CronTrigger) {
                return "cron_trigger_form";
            } else {
                return "simple_trigger_form";
            }
        }

        quartzScheduler.rescheduleJob(trigger.getKey(), form.getTrigger(trigger));

        return "redirect:/glass/jobs/{group}/{name}";
    }
}
