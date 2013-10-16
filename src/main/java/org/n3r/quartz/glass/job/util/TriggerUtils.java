package org.n3r.quartz.glass.job.util;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

public class TriggerUtils {
    public static String getPlanification(Trigger trigger) {
        if (trigger instanceof CronTrigger) {
            CronTrigger cronTrigger = (CronTrigger) trigger;

            try {
                return CronExpressionDescriptor.getDescription(cronTrigger.getCronExpression())
                        + "<br/>(" + cronTrigger.getCronExpression() + ")";
            } catch (ParseException e) {
                return cronTrigger.getCronExpression();
            }
        }

        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;

        return getPlanification(simpleTrigger.getRepeatCount(), simpleTrigger.getRepeatInterval());
    }

    public static String getPlanification(int repeatCount, long repeatInterval) {
        String planification = "";

        if (repeatCount == -1) {
            planification += "repeat forever every ";
        } else if (repeatCount == 0) {
            planification += "execute once";

            return planification;
        } else if (repeatCount == 1) {
            planification += "repeat one time in ";
        } else {
            planification += "repeat " + repeatCount + " times every ";
        }

        planification += getDurationBreakdown(repeatInterval);

        return planification;
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.MILLISECONDS.toMillis(minutes);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) sb.append(days).append(" days ");
        if (hours > 0) sb.append(hours).append(" hours ");
        if (minutes > 0) sb.append(minutes).append(" minutes ");
        if (seconds > 0) sb.append(seconds).append(" seconds ");
        if (millis > 0) sb.append(millis).append(" millis");

        return sb.toString().trim();
    }
}
