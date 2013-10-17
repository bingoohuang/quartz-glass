package org.n3r.quartz.glass.job.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.util.GlassConstants;
import org.quartz.JobDataMap;

public class JobDataMapUtils {

    public static String toProperties(JobDataMap jobDataMap) {
        StringBuilder str = new StringBuilder();

        for (String key : jobDataMap.getKeys()) {
            if (GlassConstants.METHOD_INVOKER.equals(key)) continue;
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
        return new JobDataMap( mapSplitter.split(dataMap));
    }

    public static void main(String[] args) {
        System.out.println(fromDataMapStr("orderType=退货订单\r\nlogLevel=INFO").getWrappedMap());
    }

}
