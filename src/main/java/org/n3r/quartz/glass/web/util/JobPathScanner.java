package org.n3r.quartz.glass.web.util;

import org.n3r.quartz.glass.configuration.Configuration;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scans for classes which are @GlassJob annotated.
 */
@Component
public class JobPathScanner {

    List<String> jobPaths = new ArrayList<String>();

    @Autowired
    private Configuration configuration;

    public List<String> getJobsPaths() {
        return jobPaths;
    }

    @PostConstruct
    protected void scanPaths() {
        ClassPathScanningCandidateComponentProvider provider;
        provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Job.class));

        String jobBasePackage = configuration.getJobBasePackage();
        for (BeanDefinition definition : provider.findCandidateComponents(jobBasePackage)) {
            jobPaths.add(definition.getBeanClassName());
        }

        Collections.sort(jobPaths);
    }
}
