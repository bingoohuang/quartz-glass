package org.n3r.quartz.glass.util;

import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class GlassSchedulerParserTest {
    @Test
    public void testFromTo() {
        GlassSchedulerParser parser = new GlassSchedulerParser("Every 30 minutes from 2113-10-17 21:10:00 to 2113-10-18");
        parser.parse();
        Assert.assertThat(parser.getFromDate(), is(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .parseDateTime("2113-10-17 21:10:00").toDate()));
        Assert.assertThat(parser.getToDate(), is(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .parseDateTime("2113-10-18 23:59:59").toDate()));
    }

    @Test
    public void testFrom() {
        GlassSchedulerParser parser = new GlassSchedulerParser("Every 30 minutes from 2113-10-17 21:10:00");
        parser.parse();
        Assert.assertThat(parser.getFromDate(), is(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .parseDateTime("2113-10-17 21:10:00").toDate()));
        Assert.assertThat(parser.getToDate(), nullValue());
    }

    @Test
    public void testTo() {
        GlassSchedulerParser parser = new GlassSchedulerParser("Every 30 minutes to 2113-10-18");
        parser.parse();
        Assert.assertThat(parser.getFromDate(), nullValue());
        Assert.assertThat(parser.getToDate(), is(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .parseDateTime("2113-10-18 23:59:59").toDate()));
    }
}
