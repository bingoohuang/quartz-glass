package org.n3r.quartz.glass.web.form;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;

public class JobForm {
    private String dataMap;

    public JobForm() {
    }

    public JobForm(JobDetail jobDetail) {
        this.dataMap = JobDataMapUtils.toProperties(jobDetail.getJobDataMap());
    }

    public JobDetail getJobDetails(JobDetail job) {
        return JobBuilder.newJob(job.getJobClass())
                .withIdentity(job.getKey())
                .usingJobData(JobDataMapUtils.fromDataMapStr(dataMap))
                .storeDurably()
                .build();
    }

    public String getDataMap() {
        return dataMap;
    }

    public void setDataMap(String dataMap) {
        this.dataMap = dataMap;
    }
}
