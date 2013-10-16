package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;

public class JobWrapperForView {
    private String group;
    private String name;
    private String jobKey;
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

        Class<? extends Job> jobClass1 = jobDetail.getJobClass();
        jobClass = jobClass1.getName();
        GlassJob glassJob = jobClass1.getAnnotation(GlassJob.class);
        if (glassJob != null) {
            jobDesc = glassJob.description();
            jobTeam = glassJob.team();
            jobCreated = glassJob.created();
        }

        jobDataMap = JobDataMapUtils.toProperties(jobDetail.getJobDataMap(), ", ");
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
}
