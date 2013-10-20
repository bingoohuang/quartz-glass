package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.util.Jobs;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.*;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.Method;

public class JobWrapperForView {
    private String group;
    private String name;
    private String jobKey;
    private int triggersNum;
    private String jobClass;
    private String jobDesc;
    private String jobTeam;
    private String jobCreated;
    private String jobDataMap;

    public JobWrapperForView(JobDetail jobDetail) {
        JobKey key = jobDetail.getKey();
        jobKey = Keys.desc(key);
        group = key.getGroup();
        name = key.getName();

        jobClass = Jobs.jobCass(jobDetail).getName();

        descJob(jobDetail);

        jobDataMap = JobDataMapUtils.toProperties(jobDetail.getJobDataMap());
    }

    public JobWrapperForView(Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        this(scheduler.getJobDetail(jobKey));
        triggersNum = scheduler.getTriggersOfJob(jobKey).size();
    }

    private void descJob(JobDetail jobDetail) {
        GlassJob glassJob = Jobs.glassJob(jobDetail);

        if (glassJob == null) return;

        jobDesc = glassJob.description();
        jobTeam = glassJob.team();
        jobCreated = glassJob.created();
    }


    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getJobTeam() {
        return jobTeam;
    }

    public void setJobTeam(String jobTeam) {
        this.jobTeam = jobTeam;
    }

    public String getJobCreated() {
        return jobCreated;
    }

    public void setJobCreated(String jobCreated) {
        this.jobCreated = jobCreated;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobDataMap() {
        return jobDataMap;
    }

    public void setJobDataMap(String jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    public int getTriggersNum() {
        return triggersNum;
    }
}
