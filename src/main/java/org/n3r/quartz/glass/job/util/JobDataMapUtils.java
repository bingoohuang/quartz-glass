package org.n3r.quartz.glass.job.util;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class JobDataMapUtils {

    public static String toProperties(JobDataMap jobDataMap, String separator) {
        String[] keys = jobDataMap.getKeys();

        StringBuilder stringBuilder = new StringBuilder();

        for (String key : keys) {
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(jobDataMap.getString(key));
            stringBuilder.append(separator);
        }

        return stringBuilder.toString();
    }

    public static JobDataMap fromProperties(String dataMap) {
        if (StringUtils.isEmpty(dataMap)) {
            return new JobDataMap();
        }

        Properties props = new Properties();

        try {
            props.load(new StringReader(dataMap));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JobDataMap map = new JobDataMap();

        for (Object key : props.keySet()) {
            map.put((String) key, props.getProperty((String) key));
        }

        return map;
    }

}
