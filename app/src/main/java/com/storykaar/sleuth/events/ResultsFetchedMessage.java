/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;

import java.util.Set;

/**
 * Created by pawan on 24/4/16.
 */
public class ResultsFetchedMessage {
    final public Curiosity curiosity;
    final public Set<ResultGroup> resultGroupSet;

    public ResultsFetchedMessage(Curiosity curiosity, Set<ResultGroup> resultGroupSet) {
        this.curiosity = curiosity;
        this.resultGroupSet = resultGroupSet;
    }

    @Override
    public String toString() {
        return "ResultsFetchedMessage{" +
                "curiosity=" + curiosity +
                ", resultGroupSet=" + resultGroupSet +
                '}';
    }
}
