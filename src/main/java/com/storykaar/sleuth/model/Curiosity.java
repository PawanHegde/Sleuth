/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 */
public class Curiosity
        implements Comparable<Curiosity>, Serializable {
    final public String query;

    public Curiosity(String query) {
        this.query = query.toLowerCase();
        Timber.v("Curiosity object created: %s", query);
    }

    @Override
    public String toString() {
        return "Curiosity{" +
                "query='" + query + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Curiosity curiosity = (Curiosity) o;

        return query.equalsIgnoreCase(curiosity.query);
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }

    @Override
    public int compareTo(@NonNull Curiosity another) {
        return this.query.compareToIgnoreCase(another.query);
    }

    public boolean shouldOccurBefore(@NonNull Curiosity other) {
        return this.query.compareToIgnoreCase(other.query) < 0;
    }
}
