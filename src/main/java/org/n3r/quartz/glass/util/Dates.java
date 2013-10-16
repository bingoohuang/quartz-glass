package org.n3r.quartz.glass.util;

import java.util.Date;

public class Dates {
    public static Date copy(Date date) {
        return date == null ? null : new Date(date.getTime());
    }
}
