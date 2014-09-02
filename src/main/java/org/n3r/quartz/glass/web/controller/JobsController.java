package org.n3r.quartz.glass.web.controller;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.job.annotation.JobArgumentBean;
import org.n3r.quartz.glass.job.annotation.JobBean;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.util.Keys;
import org.n3r.quartz.glass.util.Query;
import org.n3r.quartz.glass.web.form.JobForm;
import org.n3r.quartz.glass.web.form.NewJobForm;
import org.n3r.quartz.glass.web.util.JobPathScanner;
import org.n3r.quartz.glass.web.util.JobWrapperForView;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * All currently defined jobs and services around form list.
 */
@Controller
public class JobsController {
    @Autowired
    protected Scheduler scheduler;
    @Autowired
    protected Configuration configuration;
    @Autowired
    protected JobPathScanner jobPathScanner;
    @Autowired
    protected JobExecutions executions;

    @RequestMapping("/jobs")
    public String jobs(Model model) throws SchedulerException {
        List<JobWrapperForView> jobWrapperForViews = new ArrayList<JobWrapperForView>();

        List<String> groups = scheduler.getJobGroupNames();
        for (String group : groups) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                jobWrapperForViews.add(new JobWrapperForView(scheduler, jobKey));
            }
        }

        // order triggerNum desc, group, name
        Collections.sort(jobWrapperForViews, new Comparator<JobWrapperForView>() {
            @Override
            public int compare(JobWrapperForView o1, JobWrapperForView o2) {
                int diff = o2.getTriggersNum() - o1.getTriggersNum();
                if (diff != 0) return diff;

                return o1.getGroup().equals(o2.getGroup())
                        ? o1.getName().compareTo(o2.getName())
                        : o1.getGroup().compareTo(o2.getGroup());
            }
        });

        model.addAttribute("jobs", jobWrapperForViews);

        return "jobs";
    }

    @RequestMapping("/jobs/{group}/{name}")
    public String job(@PathVariable String group,
                      @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = scheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        model.addAttribute("job", new JobWrapperForView(job));
        JobBean jobBean = JobBean.fromClass(job);
        model.addAttribute("jobBean", jobBean);
        model.addAttribute("jobArguments", jobBean.getArguments());
        model.addAttribute("dataMap", JobDataMapUtils.toProperties(job.getJobDataMap()));

        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(job.getKey());

        model.addAttribute("triggers", TriggerWrapperForView.fromList(triggers, scheduler));
        model.addAttribute("history", executions.find(group, name, Query.index(0).withSize(100)));

        return "job";
    }

    @RequestMapping("/jobs/new")
    public String createJob(Model model) throws SchedulerException {
        return form(model, new NewJobForm());
    }

    @RequestMapping(value = "/jobs/new", method = RequestMethod.POST)
    public String postCreateJob(@Valid @ModelAttribute("form") NewJobForm form,
                                BindingResult bindingResult, Model model) throws SchedulerException {
        if (bindingResult.hasErrors()) return form(model, form);

        scheduler.addJob(form.getJobDetails(), true);

        return "redirect:/glass/jobs/" + form.getGroup() + "/" + form.getName();
    }

    @RequestMapping("/jobs/{group}/{name}/edit")
    public String updateJob(@PathVariable String group,
                            @PathVariable String name, Model model) throws SchedulerException {
        JobDetail job = scheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        return form(model, new JobForm(job), job.getJobClass());
    }

    @RequestMapping(value = "/jobs/{group}/{name}/edit", method = RequestMethod.POST)
    public String postUpdateJob(@PathVariable String group, @PathVariable String name,
                                @Valid @ModelAttribute("form") JobForm form,
                                BindingResult bindingResult, Model model) throws SchedulerException {
        JobDetail job = scheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        if (bindingResult.hasErrors()) return form(model, form, job.getJobClass());

        scheduler.addJob(form.getJobDetails(job), true);

        return "redirect:/glass/jobs/{group}/{name}";
    }

    @RequestMapping("/jobs/{group}/{name}/delete")
    public String delete(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        JobDetail job = scheduler.getJobDetail(new JobKey(name, group));

        if (job == null) return "redirect:/glass/jobs";

        scheduler.deleteJob(job.getKey());

        return "redirect:/glass/jobs";
    }

    @RequestMapping(value = "/jobs/{group}/{name}/fire", method = RequestMethod.POST)
    public String fire(HttpServletRequest request,
                       @PathVariable String group, @PathVariable String name) throws SchedulerException {
        JobKey jobKey = new JobKey(name, group);
        JobDetail job = scheduler.getJobDetail(jobKey);

        if (job == null) return "redirect:/glass/jobs";

        String fireJobMap = request.getParameter("fireJobMap");
        // scheduler.triggerJob(job.getKey());
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(Keys.getFireKey(), job.getKey().getGroup())
                .usingJobData(JobDataMapUtils.fromDataMapStr(fireJobMap))
                .startNow()
                .build();

        scheduler.scheduleJob(trigger);

        return "redirect:/glass/jobs/{group}/{name}";
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
