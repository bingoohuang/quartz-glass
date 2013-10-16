package org.n3r.quartz.glass.util;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlassScheduleParser {
    public static ScheduleBuilder<? extends Trigger> parse(String schedulerExpr) {
        if (StringUtils.isBlank(schedulerExpr)) throw new RuntimeException("scheduler expression can not be blank");

        String expr = schedulerExpr.trim();
        if (StringUtils.startsWithIgnoreCase(expr, "Every")) {
            return parseEveryExpr(expr.substring("Every".length()));
        } else if (StringUtils.startsWithIgnoreCase(expr, "At")) {
            return parseAtExpr(expr.substring("At".length()));
        }


        return CronScheduleBuilder
                .cronSchedule(schedulerExpr)
                .withMisfireHandlingInstructionIgnoreMisfires();
    }

    static Pattern atExprPattern = Pattern.compile(
            "\\s+\\d\\d:\\d\\d", Pattern.CASE_INSENSITIVE);
    private static ScheduleBuilder<? extends Trigger> parseAtExpr(String atExpr) {
        Matcher matcher = atExprPattern.matcher(atExpr);
        if (!matcher.matches()) throw new RuntimeException(atExpr + " is not valid");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");


        DateTime dateTime =   formatter.parseDateTime(matcher.group().trim());

        return CronScheduleBuilder.dailyAtHourAndMinute(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
    }

    static Pattern everyExprPattern = Pattern.compile(
            "\\s+(\\d+)\\s*(h|hour|m|minute|s|second)s?", Pattern.CASE_INSENSITIVE);
    private static ScheduleBuilder<? extends Trigger> parseEveryExpr(String everyExpr) {
        Matcher matcher = everyExprPattern.matcher(everyExpr);
        if (!matcher.matches()) throw new RuntimeException(everyExpr + " is not valid");
        int num = Integer.parseInt(matcher.group(1));
        if (num <= 0) throw new RuntimeException(everyExpr + " is not valid");
        char unit = matcher.group(2).charAt(0);
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        switch (unit) {
            case 'h':
            case 'H':
                timeUnit = TimeUnit.HOURS;
                break;
            case 'm':
            case 'M':
                timeUnit = TimeUnit.MINUTES;
                break;
            case 's':
            case 'S':
                timeUnit = TimeUnit.SECONDS;
                break;
            default:
        }

        return SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds((int)timeUnit.toSeconds(num))
                .repeatForever();
    }
}
