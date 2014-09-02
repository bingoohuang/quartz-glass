package org.n3r.quartz.glass.job.annotation;

import org.quartz.Scheduler;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlassTrigger {
    String name() default "";

    String group() default Scheduler.DEFAULT_GROUP;

    String scheduler();

    long startDelay() default 0;

    String triggerDataMap() default "";

    @Documented
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    static @interface List {
        GlassTrigger[] value();
    }
}
