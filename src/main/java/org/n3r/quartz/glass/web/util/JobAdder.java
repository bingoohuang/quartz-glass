package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static com.google.common.collect.ImmutableList.of;
import static org.n3r.quartz.glass.job.util.JobDataMapUtils.jobDataMapEquals;

@Component
public class JobAdder {
    @Autowired
    Scheduler quartzScheduler;

    public JobDetail createJobDetail(String jobClass) throws Exception {
        return createJobDetail(jobClass, Scheduler.DEFAULT_GROUP, "");
    }

    public JobDetail createJobDetail(String jobClass, String group, String jobDataMapStr) throws Exception {
        Class<?> defClass = Class.forName(jobClass);
        JobKey jobKey = JobKey.jobKey(Keys.nextJobIndexPostfix(defClass.getSimpleName()), group);
        JobDataMap jobDataMap = JobDataMapUtils.fromDataMapStr(jobDataMapStr);

        JobDetail jobDetail = Job.class.isAssignableFrom(defClass)
                ? createNormalJobDetail(defClass, jobKey, jobDataMap)
                : methodExecuteJobDetail(defClass, jobKey, jobDataMap);

        return addJobSmartly(jobDetail, group);
    }


    private JobDetail addJobSmartly(JobDetail thisJobDetail, String group) throws SchedulerException {
        for (String jobGroup : quartzScheduler.getJobGroupNames()) {
            if (!jobGroup.equals(group)) continue;

            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass() != thisJobDetail.getJobClass()) continue;

                MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                MethodInvoker thisInvoker = (MethodInvoker) thisJobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                // check whether the job data map is equal
                boolean jobDataMapEquals = jobDataMapEquals(jobDetail, thisJobDetail);
                if (methodInvoker == null && thisInvoker == null && jobDataMapEquals) return jobDetail;
                if (methodInvoker != null && thisInvoker != null && jobDataMapEquals
                        && methodInvoker.getTargetClass() == thisInvoker.getTargetClass()) return jobDetail;
            }
        }

        quartzScheduler.addJob(thisJobDetail, false);
        return thisJobDetail;
    }

    @SuppressWarnings("unchecked")
    private JobDetail createNormalJobDetail(Class<?> defClass, JobKey jobKey, JobDataMap jobDataMapping) {
        return JobBuilder.newJob((Class<? extends Job>) defClass)
                .withIdentity(jobKey)
                .usingJobData(jobDataMapping)
                .storeDurably()
                .build();
    }

    private JobDetail methodExecuteJobDetail(Class<?> clazz, JobKey jobKey, JobDataMap jobDataMapping) throws Exception {
        MethodInvokingJobDetailFactoryBean factoryBean = new MethodInvokingJobDetailFactoryBean();
        factoryBean.setGroup(jobKey.getGroup());
        factoryBean.setName(jobKey.getName());

        factoryBean.setTargetClass(clazz);
        factoryBean.setTargetMethod(findExecuteMethod(clazz));
        boolean allowConcurrent = !clazz.isAnnotationPresent(DisallowConcurrentExecution.class);
        factoryBean.setConcurrent(allowConcurrent);
        factoryBean.afterPropertiesSet();

        JobDetail jobDetail = factoryBean.getObject();
        jobDetail.getJobDataMap().putAll(jobDataMapping);

        return jobDetail;
    }

    private String findExecuteMethod(Class<?> defClass) {
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

        for (Method method : candidates) {
            String methodName = method.getName();
            if (of("execute", "run").contains(methodName)) return methodName;
        }

        throw new RuntimeException(defClass + " is not a valid job class");
    }

}
