/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

import com.storykaar.sleuth.model.Curiosity;

/**
 * Created by pawan on 8/8/16.
 */
public class CuriosityChangedMessage {
    public final Curiosity curiosity;
    public final Integer status;

    public CuriosityChangedMessage(Curiosity curiosity, Integer status) {
        this.curiosity = curiosity;
        this.status = status;
    }
}
