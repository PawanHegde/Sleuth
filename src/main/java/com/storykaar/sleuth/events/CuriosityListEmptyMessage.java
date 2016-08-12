/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

/**
 * Created by pawan on 7/8/16.
 *
 * Message to the activity/fragment hosting the curiosity list that
 * it is empty or not
 */
public class CuriosityListEmptyMessage {
    public final Boolean isEmpty;

    public CuriosityListEmptyMessage(Boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
}
