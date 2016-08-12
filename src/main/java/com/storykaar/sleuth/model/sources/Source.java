/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.model.sources;

import android.support.annotation.NonNull;

import com.storykaar.sleuth.R;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by pawan on 23/3/16.
 */
public class Source implements  Comparable<Source>, Serializable {
    public static final Source duckDuckGo = new Source("DuckDuckGo", R.drawable.duckduckgo);
    public static final Source wordnick = new Source("Wordnick", R.drawable.duckduckgo);
    public static final Source omdbSource = new Source("OMDb", R.drawable.duckduckgo);

    private final String name;
    private final Integer resId;

    public Source(String name, Integer resId) {
        this.name = name;
        this.resId = resId;
    }

    public String getName() {
        return this.name;
    }
    public int getResId() {
        return this.resId;
    }


    @Override
    public int compareTo(@NonNull Source another) {
        return this.getName().compareTo(another.getName());
    }

//    @Override
//    public boolean equals(@NonNull Object object) {
//        if (!(object instanceof Source)) {
//            Timber.w("%s is not of type Source", object);
//            return false;
//        }
//        boolean sameName = this.getName().equals(((Source)object).getName());
//        if (!sameName) {
//            Timber.w("%s is not he same name as %s", ((Source)object).getName(), this.getName());
//        }
//        return this.getName().equals(((Source)object).getName());
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source)) return false;
        Source source = (Source) o;
        return Objects.equals(name, source.name) &&
                Objects.equals(resId, source.resId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resId);
    }

    @Override
    public String toString() {
        return "Source{" + this.getName() + "}";
    }
}
