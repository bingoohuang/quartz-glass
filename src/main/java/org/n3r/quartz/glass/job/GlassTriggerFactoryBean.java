package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassScheduleParser;
import org.quartz.*;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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
    public void setJobDataMap(String jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @SuppressWarnings("unchecked")
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
        Class<? extends Job> jobClazz = (Class<? extends Job>) Class.forName(jobClass);
        JobDetail jobDetail = JobBuilder.newJob(jobClazz)
                .withIdentity(jobClazz.getSimpleName(), group)
                .storeDurably()
                .build();

        this.quartzScheduler.addJob(jobDetail, true);

        this.trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .startAt(startTime)
                .forJob(jobDetail)
                .withSchedule(GlassScheduleParser.parse(this.scheduler))
                .usingJobData(JobDataMapUtils.fromProperties(jobDataMap))
                .build();

        this.quartzScheduler.scheduleJob(this.trigger);
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
}
