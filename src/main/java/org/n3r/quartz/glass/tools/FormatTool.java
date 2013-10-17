package org.n3r.quartz.glass.tools;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.SpringConfig;
import org.n3r.quartz.glass.util.Jobs;
import org.n3r.quartz.glass.util.Keys;
import org.quartz.JobDetail;
import org.quartz.utils.Key;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class FormatTool {
    public static String formatDate(Date date) {
        if (date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat(SpringConfig.DATE_FORMAT);

        return sdf.format(date);
    }

    public String date(Date date) {
        return formatDate(date);
    }

    public String datePattern() {
        return SpringConfig.DATE_FORMAT;
    }

    public String html(Object object) {
        if (object == null) return "";

        return StringEscapeUtils.escapeHtml4(
                object.getClass().isArray()
                        ? Arrays.toString((Object[])object)
                        : object.toString());
    }

    public String dataMap(Object object) {
        if (object == null) return null;

        String html = html(object);
        return StringUtils.replace(html, "\n", "<br/>");
    }

    public String descKey(Key<?> key) {
        return html(Keys.desc(key));
    }

    public String jobClass(JobDetail jobDetail) {
        return Jobs.jobCass(jobDetail).getName();
    }
}