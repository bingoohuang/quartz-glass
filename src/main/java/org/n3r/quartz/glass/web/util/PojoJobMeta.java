package org.n3r.quartz.glass.web.util;

import org.quartz.JobDataMap;

public class PojoJobMeta {
    private String group;
    private String name;
    private Class<?> targetClass;
    private String targetMethod;
    private boolean concurrent;
    private JobDataMap jobDataMap;
    private Object targetObject;

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setJobDataMap(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    public JobDataMap getJobDataMap() {
        return jobDataMap;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }
}
