package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.util.GlassScheduleParser;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

public class GlassTriggerFactoryBean implements FactoryBean<Trigger>, BeanNameAware, InitializingBean {
    @Autowired
    protected Scheduler quartzScheduler;

    private String scheduler;
    private String jobClass;
    private Trigger trigger;
    private String name;
    private String group;
    private String beanName;
    private long startDelay;
    private Date startTime;
    private String triggerDataMap;
    private String jobDataMap;

    /**
     * Specify the trigger's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Specify the trigger's group.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    /**
     * Set the start delay in milliseconds.
     * <p>The start delay is added to the current system time (when the bean starts)
     * to control the start time of the trigger.
     */
    public void setStartDelay(long startDelay) {
        Assert.isTrue(startDelay >= 0, "Start delay cannot be negative");
        this.startDelay = startDelay;
    }

    /**
     * Set the trigger's JobDataMap.
     */
    public void setTriggerDataMap(String jobDataMap) {
        this.triggerDataMap = jobDataMap;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.name == null) this.name = this.beanName;
        if (this.group == null) this.group = Scheduler.DEFAULT_GROUP;

        if (this.startDelay > 0) {
            this.startTime = new Date(System.currentTimeMillis() + this.startDelay);
        } else if (this.startTime == null) {
            this.startTime = new Date();
        }

        // define the job and tie it to our HelloJob class
        Class<?> defClass = Class.forName(jobClass);
        JobKey jobKey = JobKey.jobKey(defClass.getSimpleName() + Keys.nextJobIndexPostfix(), group);
        JobDataMap jobDataMap = JobDataMapUtils.fromDataMapStr(this.jobDataMap);

        JobDetail jobDetail = Job.class.isAssignableFrom(defClass)
                ? createNormalJobDetail(defClass, jobKey, jobDataMap)
                : methodExecuterJobDetail(defClass, jobKey, jobDataMap);

        jobDetail = addJobSmartly(jobDetail);

        ScheduleBuilder<? extends Trigger> scheduleBuilder = GlassScheduleParser.parse(this.scheduler);
        JobDataMap triggerDataMap = JobDataMapUtils.fromDataMapStr(this.triggerDataMap);
        triggerDataMap.put(GlassConstants.GLASS_SCHEDULER, this.scheduler);

        this.trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .startAt(startTime)
                .forJob(jobDetail)
                .withSchedule(scheduleBuilder)
                .usingJobData(triggerDataMap)
                .build();

        this.quartzScheduler.scheduleJob(this.trigger);
    }

    private JobDetail addJobSmartly(JobDetail thisJobDetail) throws SchedulerException {
        for (String jobGroup : quartzScheduler.getJobGroupNames()) {
            if (!jobGroup.equals(this.group)) continue;

            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);
                if (jobDetail.getJobClass() != thisJobDetail.getJobClass()) continue;

                MethodInvoker methodInvoker = (MethodInvoker) jobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                MethodInvoker thisInvoker = (MethodInvoker) thisJobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
                // check whether the job datamap is equal
                if (methodInvoker == null && thisInvoker == null
                        && jobDetail.getJobDataMap().equals(thisJobDetail.getJobDataMap())) return jobDetail;
                else if (methodInvoker != null && thisInvoker != null
                        && methodInvoker.getTargetClass() == thisInvoker.getTargetClass()
                        && jobDetail.getJobDataMap().equals(thisJobDetail.getJobDataMap())) return jobDetail;
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

    private JobDetail methodExecuterJobDetail(Class<?> defClass, JobKey jobKey, JobDataMap jobDataMapping) throws Exception {
        MethodInvokingJobDetailFactoryBean factoryBean = new MethodInvokingJobDetailFactoryBean();
        factoryBean.setGroup(jobKey.getGroup());
        factoryBean.setName(jobKey.getName());
        factoryBean.setTargetObject(defClass.newInstance());
        factoryBean.setTargetMethod(findExecuteMethod(defClass));
        factoryBean.setConcurrent(false);
        factoryBean.afterPropertiesSet();

        JobDetail jobDetail = factoryBean.getObject();
        jobDetail.getJobDataMap().putAll(jobDataMapping);

        return jobDetail;
    }

    private String findExecuteMethod(Class<?> defClass) {
        Method[] declaredMethods = defClass.getDeclaredMethods();
        ArrayList<Method> candidates = new ArrayList<Method>();
        for (Method method : declaredMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("set")) continue;
            candidates.add(method);
        }

        if (candidates.size() == 1) return candidates.get(0).getName();

        ArrayList<Method> annotatedCandidates = new ArrayList<Method>();
        for (Method method : candidates) {
            if (method.getAnnotation(GlassJob.class) != null) {
                annotatedCandidates.add(method);
            }
        }

        if (annotatedCandidates.size() == 1) return annotatedCandidates.get(0).getName();

        throw new RuntimeException(defClass + " is not a valid job class");
    }

    @Override
    public Trigger getObject() throws Exception {
        return this.trigger;
    }

    @Override
    public Class<?> getObjectType() {
        return Trigger.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setQuartzScheduler(Scheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    public void setJobDataMap(String jobDataMap) {
        this.jobDataMap = jobDataMap;
    }
}
