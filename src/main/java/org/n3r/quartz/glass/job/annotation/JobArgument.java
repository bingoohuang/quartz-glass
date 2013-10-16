package org.n3r.quartz.glass.job.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface JobArgument {

    /**
     * Whether the parameter is required in GlassJob Configuration Map
     */
    boolean required() default false;

    /**
     * User friendly description of parameter
     */
    String description() default "";

    /**
     * Sample values to illustrate what kind of entry the user is expected to enter.
     */
    String[] sampleValues() default {};

}
