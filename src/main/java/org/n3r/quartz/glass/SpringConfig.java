package org.n3r.quartz.glass;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.velocity.exception.VelocityException;
import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.configuration.LogStore;
import org.n3r.quartz.glass.job.GlassJobFactory;
import org.n3r.quartz.glass.job.GlassJobListener;
import org.n3r.quartz.glass.job.GlassSchedulerListener;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.log.execution.jdbc.JdbcJobExecutions;
import org.n3r.quartz.glass.log.execution.memory.MemoryJobExecutions;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.n3r.quartz.glass.log.joblog.jdbc.JdbcJobLogStore;
import org.n3r.quartz.glass.log.joblog.memory.MemoryJobLogStore;
import org.n3r.quartz.glass.web.interceptor.AddToModelInterceptor;
import org.quartz.Scheduler;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.view.velocity.VelocityConfig;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

@org.springframework.context.annotation.Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "org.n3r.quartz.glass" })
public class SpringConfig extends WebMvcConfigurerAdapter {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String APPLICATION_CONTEXT_KEY = "applicationContext";

    @Autowired
    private GlassJobListener glassJobListener;

    @Autowired
    private GlassSchedulerListener glassSchedulerListener;

    @Autowired
    private GlassJobFactory glassJobFactory;

    @PostConstruct
    public void initLogStore() throws Exception {
        if (configuration().getLogStore() == LogStore.MEMORY) {
            JobLogs.jobLogStore = new MemoryJobLogStore();
        } else {
            JobLogs.jobLogStore = new JdbcJobLogStore(dataSource(), configuration());
        }
    }

    @Bean
    public Configuration configuration() throws Exception {
        return new Configuration();
    }

    @Bean
    public DataSource dataSource() throws Exception {
        if (configuration().isInMemory())
            return null;

        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();

        factoryBean.setJndiName("java:comp/env/jdbc/glassDb");

        factoryBean.afterPropertiesSet();

        return (DataSource) factoryBean.getObject();
    }

    @Bean
    public Scheduler quartzScheduler(ApplicationContext context) throws Exception {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setApplicationContext(context);
        factory.setExposeSchedulerInRepository(true);
        factory.setApplicationContextSchedulerContextKey(APPLICATION_CONTEXT_KEY);
        factory.setJobFactory(glassJobFactory);

        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        properties.setProperty("org.quartz.threadPool.threadCount", "15");
        properties.setProperty("org.quartz.threadPool.threadPriority", "4");

        if (configuration().isInMemory()) {
            properties.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());
        } else {
            factory.setDataSource(dataSource());

            properties.setProperty("org.quartz.jobStore.tablePrefix", configuration().getTablePrefix());
            properties.setProperty("org.quartz.jobStore.isClustered", "false");
            properties.setProperty("org.quartz.jobStore.driverDelegateClass", configuration().getDriverDelegateClass());
        }

        factory.setQuartzProperties(properties);

        factory.afterPropertiesSet();

        Scheduler scheduler = factory.getObject();

        scheduler.getListenerManager().addJobListener(glassJobListener);
        scheduler.getListenerManager().addSchedulerListener(glassSchedulerListener);

        scheduler.start();

        return scheduler;
    }

    @Bean
    public JobExecutions executions() throws Exception {
        if (configuration().getLogStore() == LogStore.MEMORY) {
            return new MemoryJobExecutions();
        } else {
            return new JdbcJobExecutions(dataSource(), configuration());
        }
    }

    @Bean
    public FixedLocaleResolver fixedLocaleResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale(Locale.FRANCE);
        return resolver;
    }

    @Bean
    public VelocityViewResolver viewResolver() {
        VelocityViewResolver viewResolver = new VelocityViewResolver();
        viewResolver.setCache(true);
        viewResolver.setPrefix("org/n3r/quartz/glass/velocity/");
        viewResolver.setSuffix(".vm");
        viewResolver.setContentType("text/html;charset=UTF-8");
        return viewResolver;
    }

    @Bean
    public VelocityConfig velocityConfig() throws IOException, VelocityException {
        Properties config = new Properties();
        config.setProperty("input.encoding", "UTF-8");
        config.setProperty("output.encoding", "UTF-8");
        config.setProperty("default.contentType", "text/html;charset=UTF-8");
        config.setProperty("resource.loader", "class");
        config.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityConfigurer velocityConfigurer = new VelocityConfigurer();
        velocityConfigurer.setVelocityProperties(config);
        velocityConfigurer.afterPropertiesSet();

        return velocityConfigurer;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public AddToModelInterceptor addToModelInterceptor() {
        return new AddToModelInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(addToModelInterceptor());
    }
}
