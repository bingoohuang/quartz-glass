package org.n3r.quartz.glass.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class QueryTest {
    private List<Long> longs = new ArrayList<Long>();

    @Before
    public void fill() {
        for (int i = 1; i <= 100; i++) {
            longs.add(Long.valueOf(i));
        }
    }

    @Test
    public void subListFirstPage() {
        Query query = Query.oneBasedIndex(1).withSize(10);

        longs = query.subList(longs);

        Assert.assertEquals("Sublist size should be 10", 10, longs.size());
        Assert.assertEquals("Fith element should be 5", Long.valueOf(5), longs.get(4));
    }

    @Test
    public void subList() {
        Query query = Query.oneBasedIndex(2).withSize(10);

        longs = query.subList(longs);

        Assert.assertEquals("Sublist size should be 10", 10, longs.size());
        Assert.assertEquals("Fith element should be 15", Long.valueOf(15), longs.get(4));
    }

    @Test
    public void subListWithSmallInitialList() {
        Query query = Query.oneBasedIndex(1).withSize(200);

        longs = query.subList(longs);

        Assert.assertEquals("Sublist size should be 100", 100, longs.size());
        Assert.assertEquals("Fith element should be 5", Long.valueOf(5), longs.get(4));
    }
}
