package org.n3r.quartz.glass.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PageTest {
    @Test
    public void count() {
        Query query = Query.oneBasedIndex(1).withSize(10);

        List<Long> longs = new ArrayList<Long>();
        for (int i = 1; i <= 45; i++) {
            longs.add(Long.valueOf(i));
        }

        Page<Long> page = Page.fromQuery(query);
        page.setItems(query.subList(longs));
        page.setTotalCount(45);

        Assert.assertEquals("There should be 5 pages", 5, page.getCount());
    }
}
