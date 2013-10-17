package org.n3r.quartz.glass.web.form;

import org.joda.time.DateTime;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.Dates;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import javax.validation.constraints.Min;
import java.text.ParseException;

public class SimpleTriggerForm extends TriggerFormSupport implements TriggerForm {
    @Min(-1)
    protected Integer repeatCount;

    @Min(0)
    protected Integer intervalInMilliseconds;

    public SimpleTriggerForm() {
    }

    public SimpleTriggerForm(Trigger trigger) {
        this.startTime = Dates.copy(trigger.getStartTime());
        this.endTime = Dates.copy(trigger.getEndTime());
        this.dataMap = JobDataMapUtils.toProperties(trigger.getJobDataMap());
        this.repeatCount = ((SimpleTrigger) trigger).getRepeatCount();
        this.intervalInMilliseconds = (int) ((SimpleTrigger) trigger).getRepeatInterval();
    }

    public Trigger getTrigger(Trigger trigger) throws ParseException {
        fixParameters();

        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().forJob(trigger.getJobKey().getName(), trigger.getJobKey().getGroup())
                .withIdentity(trigger.getKey().getName(), trigger.getKey().getGroup())
                .startAt(startTime).endAt(endTime)
                .usingJobData(JobDataMapUtils.fromDataMapStr(dataMap));

        if (repeatCount == -1) {
            builder.withSchedule(SimpleScheduleBuilder.simpleSchedule().repeatForever()
                    .withIntervalInMilliseconds(intervalInMilliseconds));
        } else {
            builder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(repeatCount)
                    .withIntervalInMilliseconds(intervalInMilliseconds));
        }

        return builder.build();
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Integer getIntervalInMilliseconds() {
        return intervalInMilliseconds;
    }

    public void setIntervalInMilliseconds(Integer intervalInMilliseconds) {
        this.intervalInMilliseconds = intervalInMilliseconds;
    }

    protected void fixParameters() {
        if (repeatCount == null) {
            repeatCount = 0;
        }

        if (intervalInMilliseconds == null) {
            intervalInMilliseconds = 1000;
        }

        if (startTime == null) {
            startTime = new DateTime().plusSeconds(1).toDate();
        }
    }
}
