package org.n3r.quartz.glass.job.util;

import org.quartz.JobExecutionContext;

/**
 * @author damien bourdette
 * @version \$Revision$
 */
public class CurrentJobExecutionContext {
    private static ThreadLocal<JobExecutionContext> threadContext = new ThreadLocal<JobExecutionContext>();

    public static JobExecutionContext get() {
        return threadContext.get();
    }

    public static void set(JobExecutionContext context) {
        threadContext.set(context);
    }

    public static void unset() {
        threadContext.set(null);
    }
}
