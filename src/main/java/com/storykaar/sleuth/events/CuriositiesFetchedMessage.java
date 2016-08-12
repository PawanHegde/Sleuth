/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

import android.support.annotation.NonNull;

import com.storykaar.sleuth.model.Curiosity;

import java.util.Map;

/**
 * Created by pawan on 24/4/16.
 */
public class CuriositiesFetchedMessage {

    @NonNull
    final public Map<Curiosity, Integer> curiosities;

    public CuriositiesFetchedMessage(Map<Curiosity, Integer> curiosities) {
        this.curiosities = curiosities;
    }

    @Override
    public String toString() {
        return "CuriositiesFetchedMessage{" +
                "curiosities=" + curiosities +
                '}';
    }
}
