package org.n3r.quartz.glass.log.execution;

public class CurrentJobExecution {
    private static ThreadLocal<JobExecution> threadExecution = new ThreadLocal<JobExecution>();

    public static JobExecution get() {
        return threadExecution.get();
    }

    public static void set(JobExecution execution) {
        threadExecution.set(execution);
    }

    public static void unset() {
        threadExecution.set(null);
    }
}
