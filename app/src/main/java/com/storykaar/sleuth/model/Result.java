/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by pawan on 23/3/16.
 */

public class Result implements Serializable, Comparable<Result> {
    public static final int GENERAL = 1;
    public static final int ANSWER = 2;
    public static final int DICTIONARY = 3;
    public static final int SHOW = 4;
    public static final int RELATED_TOPIC = 5;

    public final String image;
    public final HashMap<String, Object> propertyMap;
    public final Integer quality;
    public final Integer type;

    public Result(HashMap<String, Object> propertyMap,
                  Integer quality,
                  Integer type,
                  String image) {
        this.propertyMap = propertyMap;
        this.quality = quality;
        this.type = type;
        this.image = image;
    }

    public Result(final HashMap<String, Object> propertyMap,
                  final Integer quality, final Integer type) {
        this(propertyMap, quality, type, null);
    }

    @Override
    public String toString() {
        return "Result{" +
                "image='" + image + '\'' +
                ", propertyMap=" + propertyMap +
                ", quality=" + quality +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        System.out.println("In equals function");
        if (!(object instanceof Result)) {
            return false;
        }

        Result other = (Result) object;

        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return this.propertyMap.hashCode();
    }

    @Override
    public int compareTo(final @NonNull Result another) {
        if (this.quality != another.quality) {
            return another.quality - this.quality;
        }

        if (this.image != null || another.image != null) {
            if (this.image != null && another.image == null) {
                return 1;
            }

            if (this.image == null) {
                return -1;
            }

            if (this.image.hashCode() - another.image.hashCode() != 0) {
                return this.image.compareTo(another.image);
            }
        }

        return this.propertyMap.toString().compareTo(another.propertyMap.toString());
    }
}
