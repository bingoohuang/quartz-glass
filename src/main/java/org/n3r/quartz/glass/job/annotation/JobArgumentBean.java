package org.n3r.quartz.glass.job.annotation;

import org.codehaus.jackson.annotate.JsonProperty;
import org.n3r.quartz.glass.log.joblog.JobLogLevel;
import org.n3r.quartz.glass.util.Arrays;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean that can be used in jsp files and in json serialisations.
 *
 * @author damien bourdette
 */
public class JobArgumentBean {

    public static final String LOG_LEVEL_ARGUMENT = "logLevel";

    @JsonProperty
    String name;

    @JsonProperty
    boolean required;

    @JsonProperty
    String description;

    @JsonProperty
    String[] sampleValues;

    public static List<JobArgumentBean> fromClass(Class<?> jobClass) {
        if (jobClass == null) return null;

        List<JobArgumentBean> jobArguments = new ArrayList<JobArgumentBean>();

        for (Field field : jobClass.getDeclaredFields()) {
            JobArgument argument = field.getAnnotation(JobArgument.class);

            if (argument != null) jobArguments.add(new JobArgumentBean(field.getName(), argument));
        }

        jobArguments.add(new JobArgumentBean(LOG_LEVEL_ARGUMENT, false, "Log level used for this job.",
                new String[]{JobLogLevel.DEBUG.name(), JobLogLevel.INFO.name(), JobLogLevel.WARN.name(), JobLogLevel.ERROR.name()}));

        return jobArguments;
    }

    public JobArgumentBean() {

    }

    public JobArgumentBean(String name, JobArgument argument) {
        this.name = name;
        required = argument.required();
        description = argument.description();
        sampleValues = Arrays.copyOf(argument.sampleValues());
    }

    public JobArgumentBean(String name, boolean required, String description, String[] sampleValues) {
        this.name = name;
        this.required = required;
        this.description = description;
        this.sampleValues = Arrays.copyOf(sampleValues);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getSampleValues() {
        return Arrays.copyOf(sampleValues);
    }

    public void setSampleValues(String[] sampleValues) {
        this.sampleValues = Arrays.copyOf(sampleValues);
    }

}
