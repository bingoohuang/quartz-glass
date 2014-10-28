package org.n3r.quartz.glass.job;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.configuration.InjectionType;
import org.n3r.quartz.glass.util.GlassConstants;
import org.n3r.quartz.glass.web.util.PojoJobMeta;
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

import static org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

@Component
@SuppressWarnings("unchecked")
public class GlassJobFactory implements JobFactory {
    @Autowired
    private Configuration configuration;
    @Autowired
    AutowireCapableBeanFactory beanFactory;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        try {
            Job job = createJob(bundle);
            populateTriggerDataMapTargetObject(bundle, job);
            return job;
        } catch (Exception e) {
            throw new SchedulerException(e);
        }
    }

    private synchronized Job createJob(TriggerFiredBundle bundle) throws Exception {
        Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        try {
            Job job = beanFactory.getBean(jobClass);
            setTargetObject(job, bundle, false);
            return job;
        } catch (NoSuchBeanDefinitionException e) {
            // ignore
        }

        try {
            Job job = jobClass.newInstance();
            beanFactory.autowireBean(job);
            setTargetObject(job, bundle, true);
            return job;
        } catch (Exception e) {
            throw new SchedulerException("Problem instantiating class '" + bundle.getJobDetail().getJobClass().getName() + "'", e);
        }
    }

    private void setTargetObject(Job job, TriggerFiredBundle bundle, boolean forceCreateObject) throws Exception {
        PojoJobMeta pojoJobMeta = getPojoJobMeta(bundle.getJobDetail());
        if (pojoJobMeta == null) return;
        if (pojoJobMeta.getTargetObject() != null && !forceCreateObject) return;

        Object targetObject;
        Class targetClass = pojoJobMeta.getTargetClass();
        try {
            targetObject = beanFactory.getBean(targetClass);
        } catch (NoSuchBeanDefinitionException e) {
            targetObject = targetClass.newInstance();
            beanFactory.autowireBean(targetObject);
        }

        populateJobDataMapTargetObject(bundle, targetObject);
        pojoJobMeta.setTargetObject(targetObject);

        MethodInvokingJobDetailFactoryBean methodInvoker = createMethodInvoker(pojoJobMeta);
        methodInvoker.setTargetObject(targetObject);

        MethodInvokingJob methodInvokingJob = (MethodInvokingJob) job;
        methodInvokingJob.setMethodInvoker(methodInvoker);
    }

    private void populateJobDataMapTargetObject(TriggerFiredBundle bundle, Object targetObject) {
        MutablePropertyValues pvs = new MutablePropertyValues();

        pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());

        buildAccessor(targetObject).setPropertyValues(pvs, true);
    }

    private void populateTriggerDataMapTargetObject(TriggerFiredBundle bundle, Job job) {
        PojoJobMeta pojoJobMeta = getPojoJobMeta(bundle.getJobDetail());

        Object targetObject = pojoJobMeta == null ? job : pojoJobMeta.getTargetObject();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());
        buildAccessor(targetObject).setPropertyValues(pvs, true);
    }

    private MethodInvokingJobDetailFactoryBean createMethodInvoker(PojoJobMeta pojoJobMeta) throws Exception {
        MethodInvokingJobDetailFactoryBean factoryBean = new MethodInvokingJobDetailFactoryBean();
        factoryBean.setGroup(pojoJobMeta.getGroup());
        factoryBean.setName(pojoJobMeta.getName());

        factoryBean.setTargetClass(pojoJobMeta.getTargetClass());
        factoryBean.setTargetMethod(pojoJobMeta.getTargetMethod());
        factoryBean.setConcurrent(pojoJobMeta.isConcurrent());

        factoryBean.afterPropertiesSet();


        return factoryBean;
    }

    private PojoJobMeta getPojoJobMeta(JobDetail jobDetail) {
        return (PojoJobMeta) jobDetail.getJobDataMap().get(GlassConstants.POJO_JOB_META);
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
