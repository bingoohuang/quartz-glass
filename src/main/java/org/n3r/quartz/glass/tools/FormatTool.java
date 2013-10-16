package org.n3r.quartz.glass.tools;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.SpringConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author damien bourdette
 */
public class FormatTool {
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }

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
        if (object == null) {
            return null;
        }

        return StringEscapeUtils.escapeHtml4(object.toString());
    }

    public String dataMap(Object object) {
        if (object == null) {
            return null;
        }

        String html = html(object);

        html = StringUtils.replace(html, "\n", "<br/>");

        return html;
    }
}