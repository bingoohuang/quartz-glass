package net.redhogs.cronparser.builder;

import net.redhogs.cronparser.DateAndTimeUtils;

import java.text.MessageFormat;


/**
 * @author grhodes
 * @since 10 Dec 2012 14:18:21
 */
public class HoursDescriptionBuilder extends AbstractDescriptionBuilder {

    @Override
    protected String getSingleItemDescription(String expression) {
        return DateAndTimeUtils.formatTime(expression, "0");
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format("every {0} " + plural(expression, "hour", "hours"), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return "between {0} and {1}";
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return "at {0}";
    }

}
