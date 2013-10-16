package org.n3r.quartz.glass.job.annotation;

import org.n3r.quartz.glass.log.joblog.JobLogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Job {

    /**
     * User friendly description of job
     */
    String description();

    /**
     * Default job log level
     */
    JobLogLevel logLevel() default JobLogLevel.WARN;

}
