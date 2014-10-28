package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.job.util.JobDataMapUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.util.GlassSchedulerParser;
import org.n3r.quartz.glass.web.util.JobAdder;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Date;

public class GlassTriggerFactoryBean implements FactoryBean<Trigger>, BeanNameAware, InitializingBean {
    Logger log = LoggerFactory.getLogger(GlassTriggerFactoryBean.class);

    @Autowired
    protected Scheduler quartzScheduler;
    @Autowired
    JobAdder jobAdder;

    private String scheduler;
    private Class<?> jobClass;
    private String name;
    private long startDelay;
    private String triggerDataMap;
    private String jobDataMap;

    private String group;
    private Trigger trigger;
    private String beanName;

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

    public void setJobClass(Class<?> jobClass) {
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

    public void setJobDataMap(String jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.name == null) this.name = this.beanName;
        if (this.group == null) this.group = Scheduler.DEFAULT_GROUP;

        GlassSchedulerParser parser = new GlassSchedulerParser(this.scheduler).parse();
        if (!parser.isToDateInFuture()) {
            log.warn("ban id {}'s scheduler to date {} expired, ignored", name, parser.getToDateStr());
            return;
        }

        JobDataMap triggerDataMap = JobDataMapUtils.fromDataMapStr(this.triggerDataMap);
        triggerDataMap.put(GlassConstants.GLASS_SCHEDULER, this.scheduler);

        Date startTime = parser.getFromDate();
        if (this.startDelay > 0) {
            startTime = new Date(startTime == null
                    ? System.currentTimeMillis()
                    : (startTime.getTime() + this.startDelay));
        }

        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().withIdentity(name, group);
        if (startTime != null) builder.startAt(startTime);

        Date toDate = parser.getToDate();
        if (toDate != null) builder.endAt(toDate);

        JobDetail jobDetail = jobAdder.createJobDetail(jobClass, group, jobDataMap);
        System.out.println("JobDetail:" + System.identityHashCode(jobDetail));

        this.trigger = builder.forJob(jobDetail.getKey())
                .withSchedule(parser.getScheduleBuilder())
                .usingJobData(triggerDataMap)
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
        return false;
    }
}
