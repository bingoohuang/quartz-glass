package org.n3r.quartz.glass.web.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;

public class NewSimpleTriggerForm extends SimpleTriggerForm {
    private String group;

    private String name;

    @NotEmpty
    private String triggerGroup;

    @NotEmpty
    private String triggerName;

    public NewSimpleTriggerForm() {
    }

    public NewSimpleTriggerForm(JobDetail job) {
        this.group = job.getKey().getGroup();
        this.name = job.getKey().getName();
        this.triggerGroup = job.getKey().getGroup();
        this.triggerName = job.getKey().getName() + " trigger";
    }

    public Trigger getTrigger() throws ParseException {
        fixParameters();

        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().forJob(name.trim(), group.trim()).withIdentity(triggerName.trim(), triggerGroup.trim())
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
}
