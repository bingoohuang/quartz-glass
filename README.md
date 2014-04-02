Welcome to Quartz Glass!
=====================

A simple web ui for **quartz** and jobs simple definition on spring xml configuration.
Thank to [olagache's glass](https://github.com/olagache/glass) and [RedHogs' cron-parser](https://github.com/RedHogs/cron-parser).
This is a jar project by pom.xml defaultly and can be used in a web project like [quartz-glass-web](https://github.com/bingoohuang/quartz-glass-web).
Through the quartz-glass ui, you can list all the jobs and relatives triggers.
Jobs executions also displayed in job detail page.
And job also can be fired right now with specified trigger data map.
To see more detail, please read QuartzGlass-Intro.pdf.

The main changes include:

 1. Support Chinese in pages.
 2. Upgrade quartz to version 2.2.1 and spring to version 3.2.4.
 3. Merge job group and name into one jobkey. (when the group is default, it will not displayed)
 4. Add cron readable parser to explain what the exact the cron meaning is.
 5. Add spring xml one job bean support to simplify trigger usage.
 6. Other trivial changes.

----------

## Spring XML for a quartz-glass job examples
```xml
    <bean id="demoJobScanner" class="org.n3r.quartz.glass.web.util.JobsScanner">
        <property name="basePackage" value="org.n3r.quartz.glass.job.demo, org.n3r.demo"/>
    </bean>

    <bean id="MyJob每30分钟" class="org.n3r.quartz.glass.job.GlassTriggerFactoryBean">
        <property name="jobClass" value="org.n3r.quartz.glass.job.demo.MyJob"/>
        <property name="scheduler" value="Every 30 minutes"/>
        <property name="triggerDataMap" value="staticType=hotSale"/>
    </bean>

    <bean id="MyJob每小时开始20分钟" class="org.n3r.quartz.glass.job.GlassTriggerFactoryBean">
        <property name="jobClass" value="org.n3r.quartz.glass.job.demo.MyJob"/>
        <property name="scheduler" value="0 20 * * * ?"/>
        <property name="triggerDataMap" value="staticType=商品静态"/>
    </bean>

    <bean id="MyJob每天凌晨3点" class="org.n3r.quartz.glass.job.GlassTriggerFactoryBean">
        <property name="jobClass" value="org.n3r.quartz.glass.job.demo.MyJob"/>
        <property name="scheduler" value="At 03:00"/>
        <property name="jobDataMap" value="staticType=首页静态"/>
    </bean>

    <bean id="POJO每小时40分钟干活" class="org.n3r.quartz.glass.job.GlassTriggerFactoryBean">
        <property name="jobClass" value="org.n3r.quartz.glass.job.demo.PojoJob"/>
        <property name="scheduler" value="At ??:40"/>
        <property name="triggerDataMap" value="orderType=退货订单"/>
    </bean>

    <bean id="POJO每1分钟干活" class="org.n3r.quartz.glass.job.GlassTriggerFactoryBean">
        <property name="jobClass" value="org.n3r.quartz.glass.job.demo.PojoJob"/>
        <property name="scheduler" value="Every 1 minute"/>
        <property name="triggerDataMap" value="orderType=退货订单,logLevel=INFO"/>
    </bean>
```

## Glass Job Coding
### Normal Quartz job way:
```java
@GlassJob(description = "静态化演示任务", team = "火箭队", created = "2013-10-16")
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class MyJob implements Job {
    @JobArgument(description = "静态化类型", sampleValues = {"hotSale", "discountSale"})
    private String staticType;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JobLogs.warn("静态化类型是: " + staticType);
    }
}
```
### POJO job way:
```java
@DisallowConcurrentExecution
@GlassJob(description = "淘宝订单同步", team = "小牛队", created = "2013-10-16")
public class PojoJob {
    @JobArgument(description = "订单类型")
    private String orderType;

    public void execute() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JobLogs.info("淘宝订单:" + orderType);
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
```

## Glass scheduler expressions

| Glass Scheduler      |  Quartz Scheduler | Readable Explanation  |
| :-------- | :------:| :--: |
| **Every 30 minutes**<br/>more examples:<br/>Every 1 h/m/s  | Simple<br/>interval=30m |  repeat forever every 30 minutes   |
| **0 20 * * * ?**     |   Cron |  At 20 minutes past the hour  |
| **At 03:00**      |    Cron<br/>003?**| At 3:00:00 AM <br/>(0 0 3 ? * *)  |
| **At ??:40** |  Cron<br/>0 40 * * * ? | At 40 minutes past the hour <br/>(0 40 * * * ?) |

## Glass scheduler expressions with start and end date

| Glass Scheduler      |  Quartz Scheduler | Readable Explanation  |
| :-------- | :------:| :--: |
| **Every 30 minutes from 2013-10-10 to 2013-10-12**  | Simple<br/>interval=30m |  repeat forever every 30 minutes   |
| **0 20 * * * ? from 2013-10-10 14:00:00**     |   Cron |  At 20 minutes past the hour  |
| **At 03:00 to 2013-11-01**      |    Cron<br/>003?**| At 3:00:00 AM <br/>(0 0 3 ? * *)  |
| **At ??:40 to 2013-11-01 18:00:00** |  Cron<br/>0 40 * * * ? | At 40 minutes past the hour <br/>(0 40 * * * ?) |

## Deployment [web.xml](https://github.com/bingoohuang/quartz-glass-web/blob/master/src/main/webapp/WEB-INF/web.xml)

```
<servlet>
    <servlet-name>spring</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath*:spring/spring*.xml</param-value>
    </init-param>
</servlet>
<servlet-mapping>
    <servlet-name>spring</servlet-name>
    <url-pattern>/glass/*</url-pattern>
</servlet-mapping>

<welcome-file-list>
    <welcome-file>glass/index</welcome-file>
</welcome-file-list>
```

> **NOTE:** :
>
> - when running in jar/war, classpath*:spring*.xml does not work and the reason can be found in the comments of Spring source [PathMatchingResourcePatternResolver.java](http://docs.spring.io/spring/docs/3.2.4.RELEASE/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html) as below:

**WARNING**: Note that "classpath*:" when combined with Ant-style patterns will only work reliably with at least one root directory before the pattern starts, unless the actual target files reside in the file system. This means that a pattern like "classpath*:*.xml" will not retrieve files from the root of jar files but rather only from the root of expanded directories. This originates from a limitation in the JDK's ClassLoader.getResources() method which only returns file system locations for a passed-in empty String (indicating potential roots to search).

