/**
 * 
 */
package net.redhogs.cronparser.builder;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


/**
 * @author grhodes
 * @since 10 Dec 2012 14:23:50
 */
public class MonthDescriptionBuilder extends AbstractDescriptionBuilder {

    @Override
    protected String getSingleItemDescription(String expression) {
        if (!NumberUtils.isNumber(expression)) {
            return DateTimeFormat.forPattern("MMM").withLocale(Locale.ENGLISH).parseDateTime(expression).toString("MMMM", Locale.ENGLISH);
        }

        return new DateTime().withDayOfMonth(1).withMonthOfYear(Integer.parseInt(expression)).toString("MMMM", Locale.ENGLISH);
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", every {0} " + plural(expression, "month", "months"), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return ", {0} through {1}";
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", only in {0}";
    }

}
