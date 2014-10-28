package org.n3r.quartz.glass.util;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.web.util.PojoJobMeta;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

public class Jobs {
    public static Class<?> jobCass(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        PojoJobMeta pojoJobMeta = (PojoJobMeta) jobDataMap.get(GlassConstants.POJO_JOB_META);
        return pojoJobMeta == null ? jobDetail.getJobClass() : pojoJobMeta.getTargetClass();
    }

    public static GlassJob glassJob(JobDetail jobDetail) {
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        PojoJobMeta pojoJobMeta = (PojoJobMeta) jobDataMap.get(GlassConstants.POJO_JOB_META);
        if (pojoJobMeta == null) return jobDetail.getJobClass().getAnnotation(GlassJob.class);

        return pojoJobMeta.getTargetClass().getAnnotation(GlassJob.class);
    }


}
