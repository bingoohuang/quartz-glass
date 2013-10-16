package org.n3r.quartz.glass.tools;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.n3r.quartz.glass.job.util.TriggerUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import java.util.Date;

public class UtilsTool {
    public boolean isInterruptible(JobDetail job) {
        return InterruptableJob.class.isAssignableFrom(job.getJobClass());
    }

    public String duration(JobExecutionContext context) {
        return duration(context.getFireTime(), new Date());
    }

    public String duration(Date start, Date end) {
        Period period = new Period(start.getTime(), end.getTime());

        StringBuilder builder = new StringBuilder();

        appendDuration(builder, period.getDays(), "d");
        appendDuration(builder, period.getHours(), "h");
        appendDuration(builder, period.getMinutes(), "m");
        appendDuration(builder, period.getSeconds(), "s");

        return builder.toString().trim();
    }

    public String planification(Trigger trigger) {
        return TriggerUtils.getPlanification(trigger);
    }

    public boolean isEmpty(String string) {
        return StringUtils.isEmpty(string);
    }

    public boolean isNotEmpty(String string) {
        return StringUtils.isNotEmpty(string);
    }

    public void appendDuration(StringBuilder builder, int value, String unit) {
        if (value != 0) builder.append(value + unit + " ");
    }

    public String hash(Object object) {
        if (object == null) return "";

        return object.getClass().getSimpleName() + "-" + System.identityHashCode(object);
    }

}
