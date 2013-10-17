package org.n3r.quartz.glass.web.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.Dates;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;

public class CronTriggerForm extends TriggerFormSupport implements TriggerForm {
    @NotEmpty
    protected String cronExpression;

    public CronTriggerForm() {
    }

    public CronTriggerForm(Trigger trigger) {
        this.startTime = Dates.copy(trigger.getStartTime());
        this.endTime = Dates.copy(trigger.getEndTime());
        this.dataMap = JobDataMapUtils.toProperties(trigger.getJobDataMap());
        this.cronExpression = ((CronTrigger) trigger).getCronExpression();

    }

    public Trigger getTrigger(Trigger trigger) throws ParseException {
        fixParameters();

        return TriggerBuilder.newTrigger().forJob(trigger.getJobKey().getName(), trigger.getJobKey().getGroup())
                .withIdentity(trigger.getKey().getName(), trigger.getKey().getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionIgnoreMisfires())
                .startAt(startTime).endAt(endTime)
                .usingJobData(JobDataMapUtils.fromDataMapStr(dataMap))
                .build();
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    protected void fixParameters() {
        if (startTime == null) {
            startTime = new DateTime().plusSeconds(1).toDate();
        }
    }
}
