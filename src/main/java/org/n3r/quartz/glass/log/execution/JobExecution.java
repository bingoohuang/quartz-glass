package org.n3r.quartz.glass.log.execution;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.Dates;
import org.n3r.quartz.glass.util.Jobs;
import org.n3r.quartz.glass.util.Keys;
import org.n3r.quartz.glass.web.util.JobWrapperForView;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Date;

/**
 * Summary of a job execution stored as a log.
 */
public class JobExecution {
    private Long id;

    private Date startDate;

    private Date endDate;

    private boolean ended;

    private String jobKey;
    private String triggerKey;

    private String jobGroup;

    private String jobName;

    private String triggerGroup;

    private String triggerName;

    private String jobClass;

    private String dataMap;

    private JobExecutionResult result = JobExecutionResult.SUCCESS;

    public JobExecution() {

    }

    /**
     * Fill common attributes with properties from context.
     */
    public void fillWithContext(JobExecutionContext context) {
        startDate = context.getFireTime();

        jobClass = Jobs.jobCass(context.getJobDetail()).getName();

        JobKey key = context.getJobDetail().getKey();
        jobKey = Keys.desc(key);
        jobGroup = key.getGroup();
        jobName = key.getName();
        TriggerKey key2 = context.getTrigger().getKey();
        triggerKey = Keys.desc(key2);
        triggerGroup = key2.getGroup();
        triggerName = key2.getName();
        dataMap = JobDataMapUtils.toProperties(context.getMergedJobDataMap());
    }

    public void warn() {
        if (this.result == JobExecutionResult.ERROR) return;

        result = JobExecutionResult.WARN;
    }

    public void error() {
        result = JobExecutionResult.ERROR;
    }

    public JobExecutionResult getResult() {
        return result;
    }

    public void setResult(JobExecutionResult result) {
        if (result == null) result = JobExecutionResult.SUCCESS;

        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public void setStartDate(Date startDate) {
        this.startDate = Dates.copy(startDate);
    }

    public Date getStartDate() {
        return Dates.copy(startDate);
    }

    public Date getEndDate() {
        return Dates.copy(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = Dates.copy(endDate);
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getDataMap() {
        return dataMap;
    }

    public void setDataMap(String dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public String toString() {
        return "JobExecution{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", ended=" + ended +
                ", jobGroup='" + jobGroup + '\'' +
                ", jobName='" + jobName + '\'' +
                ", triggerGroup='" + triggerGroup + '\'' +
                ", triggerName='" + triggerName + '\'' +
                ", jobClass='" + jobClass + '\'' +
                ", dataMap='" + dataMap + '\'' +
                ", result=" + result +
                '}';
    }

    public String getTriggerKey() {
        return triggerKey;
    }

    public void setTriggerKey(String triggerKey) {
        this.triggerKey = triggerKey;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }
}
