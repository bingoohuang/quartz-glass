package org.n3r.quartz.glass.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A page of results.
 *
 * @author damien bourdette
 */
public class Page<E> {
    /**
     * Query that have been used to get this page.
     */
    private Query query;

    /**
     * All items in current page.
     */
    private List<E> items = new ArrayList<E>();

    /**
     * Total count of items in store.
     */
    private int totalCount;

    private Page() {
        super();
    }

    public static <E> Page<E> fromQuery(Query query) {
        Page<E> page = new Page<E>();

        page.query = query;

        return page;
    }

    public int getCount() {
        if (totalCount == 0) {
            return 1;
        }

        int pageCount = totalCount / query.getSize();

        if (pageCount * query.getSize() != totalCount) {
            pageCount += 1;
        }

        return pageCount;
    }

    public Query getQuery() {
        return query;
    }

    public List<E> getItems() {
        return new ArrayList<E>(items);
    }

    public void setItems(List<E> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items can not be null");
        }

        this.items = new ArrayList<E>(items);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
