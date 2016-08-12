/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.model;

import com.storykaar.sleuth.model.sources.Source;

import java.util.Set;

/**
 * Created by pawan on 7/6/16.
 */
public class ResultGroup
        implements  Comparable<ResultGroup> {
    private final Curiosity curiosity;
    private final Source source;
    private final Set<Result> results;

    public ResultGroup(Curiosity curiosity, Source source, Set<Result> results) {
        this.curiosity = curiosity;
        this.source = source;
        this.results = results;
    }

    public Curiosity getCuriosity() {
        return curiosity;
    }

    public Source getSource() {
        return source;
    }

    public Set<Result> getResults() {
        return results;
    }

    public boolean contains(final Result result) {
        return results.contains(result);
    }

    public void add(final Result result) {
        results.add(result);
    }

    public void remove(final Result result) {
        results.remove(result);
    }

    @Override
    public int compareTo(ResultGroup another) {
        if (another == null) {
            return 1;
        }

        if (!this.curiosity.equals(another.curiosity)) {
            return this.curiosity.compareTo(another.curiosity);
        }

        if (!this.source.equals(another.source)) {
            return this.source.compareTo(another.source);
        }

        return this.results.hashCode() -  another.results.hashCode();
    }

    @Override
    public String toString() {
        return "ResultGroup{" +
                "curiosity=" + curiosity +
                ", source=" + source +
                ", results=" + results +
                '}';
    }
}
