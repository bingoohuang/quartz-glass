package org.n3r.quartz.glass.web.util;

import org.quartz.JobDetail;

import java.util.List;

/**
 * Allow controllers to send a job and its associated triggers
 *
 * @author damien bourdette
 */
public class JobAndTriggers {
    private JobDetail jobDetail;

    private List<TriggerWrapperForView> triggers;

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public List<TriggerWrapperForView> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerWrapperForView> triggers) {
        this.triggers = triggers;
    }
}
