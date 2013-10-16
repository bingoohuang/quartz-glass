package org.n3r.quartz.glass.job.util;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class JobDataMapUtils {

    public static String toProperties(JobDataMap jobDataMap, String separator) {
        StringBuilder str = new StringBuilder();

        for (String key : jobDataMap.getKeys()) {
            if (str.length() > 0) str.append(separator);

            str.append(key).append("=").append(jobDataMap.getString(key));
        }

        return str.toString();
    }

    public static JobDataMap fromProperties(String dataMap) {
        if (StringUtils.isEmpty(dataMap)) return new JobDataMap();

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
