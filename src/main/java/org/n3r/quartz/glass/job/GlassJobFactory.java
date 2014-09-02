package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.configuration.InjectionType;
import org.n3r.quartz.glass.util.GlassConstants;
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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class GlassJobFactory implements JobFactory {
    @Autowired
    private Configuration configuration;
    @Autowired
    AutowireCapableBeanFactory beanFactory;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        try {
            Job job = createJob(bundle.getJobDetail());
            setProperties(bundle, job);
            return job;
        } catch (Exception e) {
            throw new SchedulerException(e);
        }
    }

    private Job createJob(JobDetail jobDetail) throws Exception {
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            Job job = beanFactory.getBean(jobClass);
            setTargetObject(jobDetail, false);
            return job;
        } catch (NoSuchBeanDefinitionException e) {
            // ignore
        }

        try {
            Job job = jobClass.newInstance();
            beanFactory.autowireBean(job);
            setTargetObject(jobDetail, true);
            return job;
        } catch (Exception e) {
            throw new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass().getName() + "'", e);
        }
    }

    private void setProperties(TriggerFiredBundle bundle, Job job) throws Exception {
        JobDetail jobDetail = bundle.getJobDetail();
        MethodInvokingJobDetailFactoryBean methodInvoker = getMethodInvokingJobDetailFactoryBean(jobDetail);

        Object targetObject = methodInvoker == null ? job : methodInvoker.getTargetObject();
        MutablePropertyValues pvs = new MutablePropertyValues();

        pvs.addPropertyValues(jobDetail.getJobDataMap());
        pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());

        buildAccessor(targetObject).setPropertyValues(pvs, true);
    }

    private void setTargetObject(JobDetail jobDetail, boolean forceCreateObject) throws Exception {
        MethodInvokingJobDetailFactoryBean methodInvoker = getMethodInvokingJobDetailFactoryBean(jobDetail);
        if (methodInvoker == null) return;
        if (methodInvoker.getTargetObject() != null && !forceCreateObject) return;

        Object targetObject;
        Class targetClass = methodInvoker.getTargetClass();
        try {
            targetObject = beanFactory.getBean(targetClass);
        } catch (NoSuchBeanDefinitionException e) {
            targetObject = targetClass.newInstance();
            beanFactory.autowireBean(targetObject);
        }

        methodInvoker.setTargetObject(targetObject);
    }

    private MethodInvokingJobDetailFactoryBean getMethodInvokingJobDetailFactoryBean(JobDetail jobDetail) {
        return (MethodInvokingJobDetailFactoryBean) jobDetail.getJobDataMap().get(GlassConstants.METHOD_INVOKER);
    }

    private AbstractPropertyAccessor buildAccessor(Object job) {
        boolean injectType = configuration.getInjectionType() == InjectionType.FIELD;
        AbstractPropertyAccessor accessor = injectType ? new DirectFieldAccessor(job) : new BeanWrapperImpl(job);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(configuration.getDateFormat());
        CustomDateEditor customDateEditor = new CustomDateEditor(simpleDateFormat, true);
        accessor.registerCustomEditor(Date.class, customDateEditor);

        return accessor;
    }
}
