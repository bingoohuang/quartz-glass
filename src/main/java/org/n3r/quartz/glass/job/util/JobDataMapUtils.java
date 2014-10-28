package org.n3r.quartz.glass.job.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.Set;

public class JobDataMapUtils {

    public static String toProperties(JobDataMap jobDataMap) {
        StringBuilder str = new StringBuilder();

        for (String key : jobDataMap.getKeys()) {
            if (GlassConstants.POJO_JOB_META.equals(key)) continue;
            if (GlassConstants.GLASS_SCHEDULER.equals(key)) continue;

            if (str.length() > 0) str.append(", ");

            str.append(key).append("=").append(jobDataMap.get(key));
        }

        return str.toString();
    }

    public static JobDataMap fromDataMapStr(String dataMap) {
        if (StringUtils.isEmpty(dataMap)) return new JobDataMap();

        Splitter.MapSplitter mapSplitter = Splitter.onPattern("[,\\n]")
                .omitEmptyStrings().trimResults().withKeyValueSeparator('=');
        return new JobDataMap(mapSplitter.split(dataMap));
    }

    public static void main(String[] args) {
        System.out.println(fromDataMapStr("orderType=退货订单\r\nlogLevel=INFO").getWrappedMap());
    }

    public static boolean jobDataMapEquals(JobDetail leftJobDetail, JobDetail rightJobDetail) {
        JobDataMap left = leftJobDetail.getJobDataMap();
        JobDataMap right = rightJobDetail.getJobDataMap();

        int leftKeys = 0;

        for (String key : left.getKeys()) {
            if (GlassConstants.POJO_JOB_META.equals(key)) continue;
            if (GlassConstants.GLASS_SCHEDULER.equals(key)) continue;

            ++leftKeys;
            if (!left.get(key).equals(right.get(key))) return false;
        }

        Set<String> rightKeySet = right.keySet();
        int rightKeys = rightKeySet.size();
        if (rightKeySet.contains(GlassConstants.POJO_JOB_META)) --rightKeys;
        if (rightKeySet.contains(GlassConstants.GLASS_SCHEDULER)) --rightKeys;

        return leftKeys == rightKeys;
    }
}
