package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.SchedulerException;
import org.quartz.listeners.SchedulerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GlassSchedulerListener extends SchedulerListenerSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogs.class);

    @Override
    public void schedulerError(String message, SchedulerException cause) {
        LOGGER.error(message, cause);
    }
}
