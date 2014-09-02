package org.n3r.quartz.glass.job.annotation;

import org.n3r.quartz.glass.log.joblog.JobLogLevel;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlassJob {

    /**
     * User friendly description of job
     */
    String description();

    /**
     * Default job log level
     */
    JobLogLevel logLevel() default JobLogLevel.INFO;

    /**
     * dev team name
     */
    String team() default "";

    /**
     * Creat date info
     */
    String created() default "";
}
