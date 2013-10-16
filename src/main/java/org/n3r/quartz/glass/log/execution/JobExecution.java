package org.n3r.quartz.glass.log.execution;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.Dates;
import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * Summary of a job execution stored as a log.
 *
 * @author damien bourdette
 */
public class JobExecution {
    private static final String KEY_IN_CONTEXT = "__GLASS_JOB_EXECUTION_CONTEXT";

    private Long id;

    private Date startDate;

    private Date endDate;

    private boolean ended;

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
        jobClass = context.getJobDetail().getJobClass().getName();
        jobGroup = context.getJobDetail().getKey().getGroup();
        jobName = context.getJobDetail().getKey().getName();
        triggerGroup = context.getTrigger().getKey().getGroup();
        triggerName = context.getTrigger().getKey().getName();
        dataMap = JobDataMapUtils.toProperties(context.getMergedJobDataMap(), "\n");
    }

    public void warn() {
        if (this.result == JobExecutionResult.ERROR) {
            return;
        }

        result = JobExecutionResult.WARN;
    }

    public void error() {
        result = JobExecutionResult.ERROR;
    }

    public JobExecutionResult getResult() {
        return result;
    }

    public void setResult(JobExecutionResult result) {
        if (result == null) {
            result = JobExecutionResult.SUCCESS;
        }

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
}
