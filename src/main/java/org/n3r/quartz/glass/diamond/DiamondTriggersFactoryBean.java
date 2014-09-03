package org.n3r.quartz.glass.diamond;

import com.alibaba.fastjson.JSON;
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

public class DiamondTriggersFactoryBean implements InitializingBean {
    Logger log = LoggerFactory.getLogger(DiamondTriggersFactoryBean.class);

    String group = "glass";
    String dataId = "triggers";

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
    }

    private void addTriggerBeans(List<TriggerBean> triggerBeans) {
        for (TriggerBean triggerBean : triggerBeans) {
            createTriggerByGlassTrigger(triggerBean);
        }
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
    private List<TriggerBean> parseTriggers(String triggers) {
        if (StringUtils.isBlank(triggers)) return Lists.newArrayList();

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
}
