/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import timber.log.Timber;

/**
 * Created by pawan on 3/8/16.
 */
public class ActionBag<T> {
    final Callable action;
    final Integer holdTime;
    final Set<T> bag = new HashSet<>(1);
    Handler handler= new Handler();

    ActionBag(@NonNull Integer holdTime, @NonNull Callable action) {
        this.holdTime = holdTime;
        this.action = action;
    }

    public void dropItems(@NonNull Set<T> objects) {
        bag.addAll(objects);
        Timber.d("%s were added to the action bag", objects.toString());

        // Delay the action on the existing items in the bag
        // Schedule the action on all of the tasks together
//        handler.removeCallbacks(action);

//        handler.postDelayed(action, holdTime);
    }

    public void dropItem(final @NonNull T object) {
        Set<T> objects = new HashSet<T>();
        objects.add(object);
        dropItems(objects);
    }

    public Set<T> peek() {
        // A new duffel bag to transport the items
        // Do not give a reference to the internal bag
        Set<T> transportBag = new HashSet<>(bag);

        return transportBag;
    }

    public void cleanBag() {
        bag.clear();
    }
}
