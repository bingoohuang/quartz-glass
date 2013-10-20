package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JobAdder {
    public static JobDetail createJobDetail(Scheduler quartzScheduler, String jobClass) throws Exception {
        return createJobDetail(quartzScheduler, jobClass, Scheduler.DEFAULT_GROUP, "");
    }

    public static JobDetail createJobDetail(Scheduler quartzScheduler, String jobClass, String group,
                                            String jobDataMapStr) throws Exception {
        Class<?> defClass = Class.forName(jobClass);
        JobKey jobKey = JobKey.jobKey(Keys.nextJobIndexPostfix(defClass.getSimpleName()), group);
        JobDataMap jobDataMap = JobDataMapUtils.fromDataMapStr(jobDataMapStr);

        JobDetail jobDetail = Job.class.isAssignableFrom(defClass)
                ? JobAdder.createNormalJobDetail(defClass, jobKey, jobDataMap)
                : JobAdder.methodExecuterJobDetail(defClass, jobKey, jobDataMap);

        return JobAdder.addJobSmartly(jobDetail, quartzScheduler, group);
    }


    private static JobDetail addJobSmartly(JobDetail thisJobDetail, Scheduler quartzScheduler, String group) throws SchedulerException {
        for (String jobGroup : quartzScheduler.getJobGroupNames()) {
            if (!jobGroup.equals(group)) continue;

            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass() != thisJobDetail.getJobClass()) continue;

                MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                MethodInvoker thisInvoker = (MethodInvoker) thisJobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                // check whether the job datamap is equal
                if (methodInvoker == null && thisInvoker == null
                        && JobDataMapUtils.jobDataMapEquals(jobDetail, thisJobDetail))
                    return jobDetail;
                else if (methodInvoker != null && thisInvoker != null
                        && methodInvoker.getTargetClass() == thisInvoker.getTargetClass()
                        && JobDataMapUtils.jobDataMapEquals(jobDetail, thisJobDetail))
                    return jobDetail;
            }
        }

        quartzScheduler.addJob(thisJobDetail, false);
        return thisJobDetail;
    }

    @SuppressWarnings("unchecked")
    private static JobDetail createNormalJobDetail(Class<?> defClass, JobKey jobKey, JobDataMap jobDataMapping) {
        return JobBuilder.newJob((Class<? extends Job>) defClass)
                .withIdentity(jobKey)
                .usingJobData(jobDataMapping)
                .storeDurably()
                .build();
    }

    private static JobDetail methodExecuterJobDetail(Class<?> defClass, JobKey jobKey, JobDataMap jobDataMapping) throws Exception {
        MethodInvokingJobDetailFactoryBean factoryBean = new MethodInvokingJobDetailFactoryBean();
        factoryBean.setGroup(jobKey.getGroup());
        factoryBean.setName(jobKey.getName());
        factoryBean.setTargetObject(defClass.newInstance());
        factoryBean.setTargetMethod(findExecuteMethod(defClass));
        factoryBean.setConcurrent(!defClass.isAnnotationPresent(DisallowConcurrentExecution.class));
        factoryBean.afterPropertiesSet();

        JobDetail jobDetail = factoryBean.getObject();
        jobDetail.getJobDataMap().putAll(jobDataMapping);

        return jobDetail;
    }

    private static String findExecuteMethod(Class<?> defClass) {
        ArrayList<Method> candidates = new ArrayList<Method>();
        for (Method method : defClass.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("set")) continue; // not setter/getter
            if (!Modifier.isPublic(method.getModifiers())) continue; // should be public
            if (Modifier.isStatic(method.getModifiers())) continue; // non static
            if (method.getParameterTypes().length > 0) continue; // no parameters

            candidates.add(method);
        }

        if (candidates.size() == 1) return candidates.get(0).getName();

        ArrayList<Method> annotatedCandidates = new ArrayList<Method>();
        for (Method method : candidates) {
            if (method.getAnnotation(GlassJob.class) == null) continue;
            annotatedCandidates.add(method);
        }

        if (annotatedCandidates.size() == 1) return annotatedCandidates.get(0).getName();

        throw new RuntimeException(defClass + " is not a valid job class");
    }

}
