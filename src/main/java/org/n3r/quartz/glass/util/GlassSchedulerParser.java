package org.n3r.quartz.glass.util;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlassSchedulerParser {
    static Pattern fromPattern = Pattern.compile(
            "\\bfrom\\b\\s*(\\d\\d\\d\\d-\\d\\d-\\d\\d)( \\d\\d:\\d\\d:\\d\\d)?", Pattern.CASE_INSENSITIVE);
    static Pattern toPattern = Pattern.compile(
            "\\bto\\b\\s*(\\d\\d\\d\\d-\\d\\d-\\d\\d)( \\d\\d:\\d\\d:\\d\\d)?", Pattern.CASE_INSENSITIVE);
    private final String schedulerExpr;
    private String expr;
    private Date fromDate;
    private Date toDate;
    private ScheduleBuilder<? extends Trigger> scheduleBuilder;

    public GlassSchedulerParser(String schedulerExpr) {
        if (StringUtils.isBlank(schedulerExpr))
            throw new RuntimeException("scheduler expression can not be blank");

        this.schedulerExpr = schedulerExpr;
        this.expr = schedulerExpr.trim();
    }

    public GlassSchedulerParser parse() {
        fromDate = parseDate(fromPattern, "00:00:00");
        if (fromDate != null && fromDate.before(new Date())) fromDate = null;

        toDate = parseDate(toPattern, "23:59:59");
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new RuntimeException("scheduler expression is not valid because of from date is after of to date");
        }

        if (StringUtils.startsWithIgnoreCase(expr, "Every")) {
            this.scheduleBuilder = parseEveryExpr(expr.substring("Every".length()));
        } else if (StringUtils.startsWithIgnoreCase(expr, "At")) {
            this.scheduleBuilder =  parseAtExpr(expr.substring("At".length()));
        } else {
            this.scheduleBuilder = parseCron();
        }

        return this;
    }

    private Date parseDate(Pattern pattern, String defaultTime) {
        Matcher fromMatcher = pattern.matcher(expr);
        if (!fromMatcher.find()) return null;

        String fromDay = fromMatcher.group(1);
        String timePart = fromMatcher.group(2);
        String fromTime = timePart == null ? defaultTime : timePart.trim();

        expr = StringUtils.substring(expr, 0, fromMatcher.start())
                + StringUtils.substring(expr, fromMatcher.end());
        expr = expr.trim();

        return DateTimeFormat.forPattern("yyyy-MM-ddHH:mm:ss")
                .parseDateTime(fromDay + fromTime).toDate();
    }

    private CronScheduleBuilder parseCron() {
        return CronScheduleBuilder
                .cronSchedule(schedulerExpr)
                .withMisfireHandlingInstructionIgnoreMisfires();
    }

    static Pattern atExprPattern = Pattern.compile(
            "\\s+(\\d\\d|\\?\\?):(\\d\\d)", Pattern.CASE_INSENSITIVE);

    private static ScheduleBuilder<? extends Trigger> parseAtExpr(String atExpr) {
        Matcher matcher = atExprPattern.matcher(atExpr);
        if (!matcher.matches()) throw new RuntimeException(atExpr + " is not valid");

        if (matcher.group(1).equals("??")) {
            return CronScheduleBuilder.cronSchedule("0 " + matcher.group(2) + " * * * ?");
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
        DateTime dateTime = formatter.parseDateTime(matcher.group().trim());

        return CronScheduleBuilder.dailyAtHourAndMinute(dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour());
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
                .withIntervalInSeconds((int) timeUnit.toSeconds(num))
                .repeatForever();
    }

    public boolean isToDateInFuture() {
        return toDate == null || toDate.after(new Date());
    }

    public String getToDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(toDate);
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public ScheduleBuilder<? extends Trigger> getScheduleBuilder() {
        return scheduleBuilder;
    }
}
