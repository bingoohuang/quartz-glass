package org.n3r.quartz.glass.web.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;

import javax.validation.constraints.NotNull;

/**
 * Form for job editing
 */
public class NewJobForm {

    @NotEmpty
    private String group = "DEFAULT";

    @NotEmpty
    private String name;

    @NotNull
    private Class<? extends Job> clazz;

    private String dataMap;

    public NewJobForm() {
    }

    public NewJobForm(JobDetail jobDetail) {
        super();
        this.group = jobDetail.getKey().getGroup();
        this.name = jobDetail.getKey().getName();
        this.clazz = jobDetail.getJobClass();
        this.dataMap = JobDataMapUtils.toProperties(jobDetail.getJobDataMap());
    }

    /**
     * Builds a {@link JobDetail} using internal state
     *
     * @return
     */
    public JobDetail getJobDetails() {
        return JobBuilder.newJob(clazz)
                .withIdentity(name.trim(), group.trim())
                .usingJobData(JobDataMapUtils.fromDataMapStr(dataMap))
                .storeDurably()
                .build();
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

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends Job> clazz) {
        this.clazz = clazz;
    }

    public String getDataMap() {
        return dataMap;
    }

    public void setDataMap(String dataMap) {
        this.dataMap = dataMap;
    }

}
