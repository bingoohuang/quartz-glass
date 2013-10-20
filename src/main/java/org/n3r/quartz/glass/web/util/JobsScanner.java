package org.n3r.quartz.glass.web.util;

import com.google.common.base.Splitter;
import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.annotation.PostConstruct;

public class JobsScanner {
    private String basePackage;

    @Autowired
    protected Scheduler scheduler;

    @PostConstruct
    protected void scanPaths() throws Exception {
        ClassPathScanningCandidateComponentProvider provider;
        provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Job.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(GlassJob.class));

        Iterable<String> basePackages = Splitter.on(',').omitEmptyStrings().trimResults().split(basePackage);
        for (String bp : basePackages) {
            for (BeanDefinition definition : provider.findCandidateComponents(bp)) {
                JobAdder.createJobDetail(scheduler, definition.getBeanClassName());
            }
        }
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
