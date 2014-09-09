package org.n3r.quartz.glass.web.util;

import com.google.common.base.Splitter;
import com.google.common.primitives.UnsignedInts;
import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.job.GlassTriggerFactoryBean;
import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.GlassTrigger;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.annotation.PostConstruct;
import java.util.UUID;

public class JobsScanner {
    Logger log = LoggerFactory.getLogger(JobsScanner.class);
    String basePackage;

    @Autowired
    JobAdder jobAdder;
    @Autowired
    AutowireCapableBeanFactory beanFactory;

    @PostConstruct
    protected void scanPaths() {
        ClassPathScanningCandidateComponentProvider provider;
        provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Job.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(GlassJob.class));

        try {
            Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
            for (String bp : splitter.split(basePackage)) {
                for (BeanDefinition definition : provider.findCandidateComponents(bp)) {
                    String beanClassName = definition.getBeanClassName();
                    Class<?> jobClass = Class.forName(beanClassName);
                    jobAdder.createJobDetail(jobClass);

                    createTriggerByGlassTrigger(jobClass);
                }
            }
        } catch (Exception ex) {
            log.warn("scan jobs error", ex);
        }
    }

    private void createTriggerByGlassTrigger(Class<?> clazz) throws Exception {
        GlassTrigger glassTrigger = clazz.getAnnotation(GlassTrigger.class);
        if (glassTrigger != null) createTriggerByGlassTrigger(clazz, glassTrigger);

        GlassTrigger.List glassTriggers = clazz.getAnnotation(GlassTrigger.List.class);
        if (glassTriggers != null) {
            for (GlassTrigger trigger : glassTriggers.value()) {
                createTriggerByGlassTrigger(clazz, trigger);
            }
        }
    }

    private void createTriggerByGlassTrigger(Class<?> clazz, GlassTrigger glassTrigger) throws Exception {
        GlassTriggerFactoryBean factoryBean = new GlassTriggerFactoryBean();
        if (StringUtils.isBlank(glassTrigger.name())) {
            factoryBean.setName("Auto@" + UnsignedInts.toString(UUID.randomUUID().hashCode()));
        } else {
            factoryBean.setName(glassTrigger.name());
        }

        factoryBean.setGroup(glassTrigger.group());
        factoryBean.setJobClass(clazz);
        factoryBean.setTriggerDataMap(glassTrigger.triggerDataMap());
        factoryBean.setScheduler(glassTrigger.scheduler());
        factoryBean.setStartDelay(glassTrigger.startDelay());
        beanFactory.autowireBean(factoryBean);

        factoryBean.afterPropertiesSet();
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
