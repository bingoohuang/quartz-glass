package org.n3r.quartz.glass.util;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.Method;

public class Jobs {
    public static Class<?> jobCass(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        MethodInvoker methodInvoker = (MethodInvoker) jobDataMap.get(GlassConstants.METHOD_INVOKER);
        return methodInvoker == null ? jobDetail.getJobClass() : methodInvoker.getTargetClass();
    }

    public static GlassJob glassJob(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        MethodInvoker methodInvoker = (MethodInvoker) jobDataMap.get(GlassConstants.METHOD_INVOKER);
        if (methodInvoker == null) return jobDetail.getJobClass().getAnnotation(GlassJob.class);

        Method targetMethod = methodInvoker.getPreparedMethod();
        GlassJob glassJob = targetMethod.getAnnotation(GlassJob.class);
        if (glassJob != null) return glassJob;

        return targetMethod.getDeclaringClass().getAnnotation(GlassJob.class);
    }


}
