/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by pawan on 6/7/16.
 *
 * A bag to hold items for at least a specified time
 * The bag is purged after this time is complete
 * Time is reset after any new addition
 * So if multiple deletions are done in the retain time,
 * they are all purged retain time after the last addition
 */
public class PurgeableBag<T> {
    final Integer retainTime;
    final ExitTask exitTask;
    final Set<T> bag = new HashSet<>(1);
    Timer timer = new Timer();

    PurgeableBag(@NonNull Integer retainTime, PurgeableBag.ExitTask exitTask) {
        this.retainTime = retainTime;
        this.exitTask = exitTask;
    }

    public void dropItem(@NonNull T object) {
        bag.add(object);
        Timber.d("%s added to the purgeable bag", object.toString());

        // Delay purging the existing items in the bag
        // Schedule a deletion of all tasks together
        timer.cancel();

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Timber.d("PurgeableBag purged of %s", bag);

                for (T t: bag) {
                    exitTask.item = t;
                    AsyncTask.execute(exitTask);
                }

                bag.clear();
            }
        }, retainTime);
    }

    public void dropItems(@NonNull Set<T> objects) {
        bag.addAll(objects);
        Timber.d("%s were added to the purgeable bag", objects.toString());

        // Delay purging the existing items in the bag
        // Schedule a deletion of all tasks together
        timer.cancel();

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Timber.d("PurgeableBag purged of %s", bag);
                bag.clear();
                AsyncTask.execute(exitTask);
            }
        }, retainTime);
    }

    public Set<T> retrieveItems() {
        // A new duffel bag to transport the items
        // which were saved from deletion
        // Do not give a reference to the internal bag
        Set<T> transportBag = new HashSet<>(bag);
        Timber.d("%s were saved from the purgeable bag", transportBag);

        timer.cancel();
        bag.clear();

        return transportBag;
    }

    public static abstract class ExitTask<T> implements Runnable {
        public T item;
    }
}
