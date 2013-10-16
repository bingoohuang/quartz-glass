package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.configuration.InjectionType;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.AbstractPropertyAccessor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author damien bourdette
 */
@Component
public class GlassJobFactory implements JobFactory {
    @Autowired
    private Configuration configuration;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Job job = createJob(bundle.getJobDetail());

        setProperties(bundle, job);

        return job;
    }

    private Job createJob(JobDetail jobDetail) throws SchedulerException {
        Class<? extends Job> jobClass = jobDetail.getJobClass();

        try {
            return jobClass.newInstance();
        } catch (Exception e) {
            throw new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass().getName() + "'", e);
        }
    }

    private void setProperties(TriggerFiredBundle bundle, Job job) {
        MutablePropertyValues pvs = new MutablePropertyValues();

        pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());
        pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());

        buildAccessor(job).setPropertyValues(pvs, true);
    }

    private AbstractPropertyAccessor buildAccessor(Job job) {
        AbstractPropertyAccessor accessor = null;

        if (configuration.getInjectionType() == InjectionType.FIELD) {
            accessor = new DirectFieldAccessor(job);
        } else {
            accessor = new BeanWrapperImpl(job);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(configuration.getDateFormat());
        CustomDateEditor customDateEditor = new CustomDateEditor(simpleDateFormat, true);
        accessor.registerCustomEditor(Date.class, customDateEditor);

        return accessor;
    }
}
