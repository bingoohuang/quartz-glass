package org.n3r.quartz.glass.util;

import org.n3r.quartz.glass.log.execution.JobExecutionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for paged queries in services.
 *
 * @author damien bourdette
 */
public class Query {
    public static final int DEFAULT_SIZE = 100;

    /**
     * 0 based page index
     */
    private int index;

    /**
     * Size of a page
     */
    private int size = DEFAULT_SIZE;

    private JobExecutionResult result;

    private Query() {

    }

    public static Query index(int index) {
        if (index < 0) {
            index = 0;
        }

        Query query = new Query();

        query.index = index;

        return query;
    }

    public static Query oneBasedIndex(int index) {
        return index(index - 1);
    }

    public static Query firstPage() {
        return index(0);
    }

    public Query withSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("size must be stricly positive");
        }

        Query query = copy();

        query.size = size;

        return query;
    }

    public Query withResult(JobExecutionResult result) {
        Query query = copy();

        query.result = result;

        return query;
    }

    public Query withResult(String result) {
        return withResult(JobExecutionResult.valueOf(result.toUpperCase()));
    }

    public <T> List<T> subList(List<T> list) {
        int start = Math.min(getStart(), list.size());
        int end = Math.min(getEnd(), list.size());

        return new ArrayList<T>(list.subList(start, end));
    }

    public String applySqlLimit(String sql) {
        StringBuilder builder = new StringBuilder();

        if (getStart() != 0) {
            builder.append("select * from ( select row_.*, rownum rownum_ from ( ");
        } else {
            builder.append("select * from ( ");
        }

        builder.append(sql);

        if (getStart() != 0) {
            builder.append(" ) row_ where rownum <= " + getEnd() + ") where rownum_ > " + +getStart());
        } else {
            builder.append(" ) where rownum <= " + getEnd());
        }

        return builder.toString();
    }

    public int getIndex() {
        return index;
    }

    public int getOneBasedIndex() {
        return index + 1;
    }

    public JobExecutionResult getResult() {
        return result;
    }

    public int getSize() {
        return size;
    }

    public int getStart() {
        return index * size;
    }

    public int getEnd() {
        return getStart() + size;
    }

    private Query copy() {
        Query query = new Query();

        query.index = index;
        query.size = size;
        query.result = result;

        return query;
    }
}
