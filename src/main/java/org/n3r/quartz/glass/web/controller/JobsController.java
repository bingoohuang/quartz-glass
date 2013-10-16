package org.n3r.quartz.glass.web.controller;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.job.annotation.JobArgumentBean;
import org.n3r.quartz.glass.job.annotation.JobBean;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.util.Query;
import org.n3r.quartz.glass.web.form.JobForm;
import org.n3r.quartz.glass.web.form.NewJobForm;
import org.n3r.quartz.glass.web.util.JobPathScanner;
import org.n3r.quartz.glass.web.util.TriggerWrapperForView;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * All currently defined jobs and services around form list.
 */
@Controller
public class JobsController {

    @Autowired
    protected Scheduler quartzScheduler;

    @Autowired
    protected Configuration configuration;

    @Autowired
    protected JobPathScanner jobPathScanner;

    @Autowired
    protected JobExecutions executions;

    @RequestMapping("/jobs")
    public String jobs(Model model) throws SchedulerException {
        List<JobDetail> jobs = new ArrayList<JobDetail>();

        List<String> groups = quartzScheduler.getJobGroupNames();
        Collections.sort(groups);

        for (String group : groups) {
            Set<JobKey> jobKeys = quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group));

            for (JobKey jobKey : jobKeys) {
                jobs.add(quartzScheduler.getJobDetail(jobKey));
            }
        }

        model.addAttribute("jobs", jobs);

        return "jobs";
    }

    @RequestMapping("/jobs/{group}/{name}")
    public String job(@PathVariable String group, @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:" + configuration.getRoot() + "/jobs";

        model.addAttribute("job", job);
        model.addAttribute("jobBean", JobBean.fromClass(job.getJobClass()));
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(job.getJobClass()));
        model.addAttribute("dataMap", JobDataMapUtils.toProperties(job.getJobDataMap(), "\n"));

        List<? extends Trigger> triggers = quartzScheduler.getTriggersOfJob(job.getKey());

        model.addAttribute("triggers", TriggerWrapperForView.fromList(triggers, quartzScheduler));

        model.addAttribute("history", executions.find(group, name, Query.index(0).withSize(5)));

        return "job";
    }

    @RequestMapping("/jobs/new")
    public String createJob(Model model) throws SchedulerException {
        return form(model, new NewJobForm());
    }

    @RequestMapping(value = "/jobs/new", method = RequestMethod.POST)
    public String postCreateJob(@Valid @ModelAttribute("form") NewJobForm form, BindingResult bindingResult, Model model) throws SchedulerException {
        if (bindingResult.hasErrors()) return form(model, form);

        quartzScheduler.addJob(form.getJobDetails(), true);

        return "redirect:" + configuration.getRoot() + "/jobs/" + form.getGroup() + "/" + form.getName();
    }

    @RequestMapping("/jobs/{group}/{name}/edit")
    public String updateJob(@PathVariable String group, @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:" + configuration.getRoot() + "/jobs";

        return form(model, new JobForm(job), job.getJobClass());
    }

    @RequestMapping(value = "/jobs/{group}/{name}/edit", method = RequestMethod.POST)
    public String postUpdateJob(@PathVariable String group, @PathVariable String name, @Valid @ModelAttribute("form") JobForm form, BindingResult bindingResult, Model model) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:" + configuration.getRoot() + "/jobs";

        if (bindingResult.hasErrors()) {
            return form(model, form, job.getJobClass());
        }

        quartzScheduler.addJob(form.getJobDetails(job), true);

        return "redirect:" + configuration.getRoot() + "/jobs/{group}/{name}";
    }

    @RequestMapping("/jobs/{group}/{name}/delete")
    public String delete(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:" + configuration.getRoot() + "/jobs";

        quartzScheduler.deleteJob(job.getKey());

        return "redirect:" + configuration.getRoot() + "/jobs";
    }

    @RequestMapping("/jobs/{group}/{name}/fire")
    public String fire(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        JobDetail job = quartzScheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:" + configuration.getRoot() + "/jobs";

        quartzScheduler.triggerJob(job.getKey());

        return "redirect:" + configuration.getRoot() + "/jobs/{group}/{name}";
    }

    private String form(Model model, NewJobForm form) {
        List<Class<?>> jobClasses = new ArrayList<Class<?>>();

        for (String jobPath : jobPathScanner.getJobsPaths()) {
            try {
                jobClasses.add(Class.forName(jobPath));
            } catch (ClassNotFoundException e) {
                continue;
            }
        }

        model.addAttribute("jobClasses", jobClasses);
        model.addAttribute("jobBean", JobBean.fromClass(form.getClazz()));
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(form.getClazz()));
        model.addAttribute("form", form);

        return "new_job_form";
    }

    private String form(Model model, JobForm form, Class<?> jobClass) {
        model.addAttribute("jobClass", jobClass);
        model.addAttribute("jobBean", JobBean.fromClass(jobClass));
        model.addAttribute("jobArguments", JobArgumentBean.fromClass(jobClass));
        model.addAttribute("form", form);

        return "job_form";
    }
}
