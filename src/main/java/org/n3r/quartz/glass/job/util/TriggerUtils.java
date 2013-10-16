package org.n3r.quartz.glass.job.util;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author damien bourdette
 */
public class TriggerUtils {
    public static String getPlanification(Trigger trigger) {
        if (trigger instanceof CronTrigger) {
            CronTrigger cronTrigger = (CronTrigger) trigger;

            return cronTrigger.getCronExpression();
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

        planification += repeatInterval + "ms";

        return planification;
    }
}
