package org.n3r.quartz.glass.diamond;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondListenerAdapter;
import org.n3r.diamond.client.DiamondManager;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.quartz.glass.job.GlassTriggerFactoryBean;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.*;

public class DiamondTriggersFactoryBean implements InitializingBean {
    Logger log = LoggerFactory.getLogger(DiamondTriggersFactoryBean.class);

    String group = "glass";
    String dataId = "triggers";
    String format = "json";

    @Autowired
    AutowireCapableBeanFactory beanFactory;
    @Autowired
    Scheduler quartzScheduler;

    List<Trigger> triggers = Lists.newArrayList();


    @Override
    public void afterPropertiesSet() throws Exception {
        DiamondManager diamondManager = new DiamondManager(getGroup(), getDataId());
        String triggers = diamondManager.getDiamond();
        List<TriggerBean> triggerBeans = parseTriggers(triggers);
        addTriggerBeans(triggerBeans);

        diamondManager.addDiamondListener(new DiamondListenerAdapter() {
            @Override
            public void accept(DiamondStone diamondStone) {
                deleteOldTriggers();

                String triggers = diamondStone.getContent();
                List<TriggerBean> triggerBeans = parseTriggers(triggers);
                addTriggerBeans(triggerBeans);
            }
        });
    }

    private void deleteOldTriggers() {
        for (Trigger trigger : triggers) {
            try {
                quartzScheduler.unscheduleJob(trigger.getKey());
            } catch (SchedulerException e) {
                log.warn("unschedule job error", e);
            }
        }
        triggers.clear();
    }

    private void addTriggerBeans(List<TriggerBean> triggerBeans) {
        for (TriggerBean triggerBean : triggerBeans) {
            createTriggerByGlassTrigger(triggerBean);
        }
    }


    private List<TriggerBean> parseTriggers(String triggers) {
        if (StringUtils.isBlank(triggers)) return Lists.newArrayList();

        if ("JSON".equalsIgnoreCase(format)) return parseJSON(triggers);
        if ("PROP".equalsIgnoreCase(format)) return parseProp(triggers);

        throw new RuntimeException("unknown format " + format + ", only JSON or PROP allowed");
    }

    /*
      trigger1.name: Diamond-MyJob每3秒执行
      trigger1.scheduler: Every 3 seconds
      trigger1.jobClass: org.n3r.demo.MyJob
      trigger1.triggerDataMap: staticType=商品静态

      trigger2.name: Diamond-PojoJob每3秒执行
      trigger2.scheduler: Every 3 seconds
      trigger2.jobClass: org.n3r.demo.PojoJob

      trigger3.scheduler: Every 5 seconds
      trigger3.jobClass: org.n3r.demo.PojoJob
     */
    private List<TriggerBean> parseProp(String triggers) {
        Splitter splitter = Splitter.on('\n').omitEmptyStrings().trimResults();
        String lastTriggerId = null;
        TriggerBean triggerBean = null;
        List<TriggerBean> triggerBeans = Lists.newArrayList();

        for (String line : splitter.split(triggers)) {
            if (line.startsWith("#")) continue;

            int keyPos = indexOfAny(line, ':', '=');
            if (keyPos < 0) continue;

            String key = trim(substring(line, 0, keyPos));
            if (isBlank(key)) continue;

            String value = trim(substring(line, keyPos + 1));
            if (isBlank(value)) continue;

            int lastDotPos = key.lastIndexOf('.');
            if (lastDotPos < 0) continue;

            String triggerId = key.substring(0, lastDotPos);
            String property = key.substring(lastDotPos + 1);

            if (!triggerId.equalsIgnoreCase(lastTriggerId)) {
                lastTriggerId = triggerId;

                triggerBean = new TriggerBean();
                triggerBeans.add(triggerBean);
            }

            if ("name".equalsIgnoreCase(property)) {
                triggerBean.setName(value);
            } else if ("scheduler".equalsIgnoreCase(property)) {
                triggerBean.setScheduler(value);
            } else if ("jobClass".equalsIgnoreCase(property)) {
                triggerBean.setJobClass(value);
            } else if ("triggerDataMap".equalsIgnoreCase(property)) {
                triggerBean.setTriggerDataMap(value);
            } else if ("startDelay".equalsIgnoreCase(property)) {
                triggerBean.setStartDelay(Long.parseLong(value));
            }
        }

        return triggerBeans;
    }

    /* parse trigger json array like following:
       [
         {
           name: "Diamond-MyJob每3秒执行",
           scheduler: "Every 3 seconds",
           jobClass: "org.n3r.demo.MyJob",
           triggerDataMap: "staticType=商品静态"
         },
         {
           name: "Diamond-PojoJob每3秒执行",
           scheduler: "Every 3 seconds",
           jobClass: "org.n3r.demo.PojoJob"
         },
         {
           scheduler: "Every 5 seconds",
           jobClass: "org.n3r.demo.PojoJob"
         }
       ]
    */
    private List<TriggerBean> parseJSON(String triggers) {
        return JSON.parseArray(triggers, TriggerBean.class);
    }

    private void createTriggerByGlassTrigger(TriggerBean glassTrigger) {
        GlassTriggerFactoryBean factoryBean = new GlassTriggerFactoryBean();
        if (StringUtils.isBlank(glassTrigger.getName())) {
            factoryBean.setName("Auto@" + UUID.randomUUID().hashCode());
        } else {
            factoryBean.setName(glassTrigger.getName());
        }

        try {
            factoryBean.setGroup(glassTrigger.getGroup());
            Class<?> clazz = Class.forName(glassTrigger.getJobClass());
            factoryBean.setJobClass(clazz);
            factoryBean.setTriggerDataMap(glassTrigger.getTriggerDataMap());
            factoryBean.setScheduler(glassTrigger.getScheduler());
            factoryBean.setStartDelay(glassTrigger.getStartDelay());
            beanFactory.autowireBean(factoryBean);

            factoryBean.afterPropertiesSet();
            Trigger trigger = factoryBean.getObject();
            triggers.add(trigger);
        } catch (Exception e) {
            log.warn("createTriggerByGlassTrigger error", e);
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
