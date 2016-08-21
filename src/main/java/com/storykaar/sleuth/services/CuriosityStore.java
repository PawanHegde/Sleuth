/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.support.annotation.NonNull;

import com.storykaar.sleuth.Constants;
import com.storykaar.sleuth.events.CuriositiesFetchedMessage;
import com.storykaar.sleuth.events.CuriosityChangedMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.services.storage.StorageController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 *
 * Central hub of Curiosities' backend
 */
public class CuriosityStore {
    private static CuriosityStore instance = new CuriosityStore();

    private final HashMap<Curiosity, Integer> curiosities = new HashMap<>();
    private final PurgeableBag<Map<Curiosity, Integer>> undoableCuriosities = new PurgeableBag<>(3000, new PurgeableBag.ExitTask<Map<Curiosity, Integer>>() {
        @Override
        public void run() {
            // TODO: Formulate a general way to stop any pending downloads and delete results
            for (Curiosity curiosity: item.keySet()) {
                ResultStore.getResultStore().deleteResultsFor(curiosity);
            }
        }
    });

    public static CuriosityStore getInstance() {
        return instance;
    }

    public void requestCuriosities() {
        if (curiosities.isEmpty()) {
            Timber.d("Requesting curiosities from storage controller");
            StorageController.requestCuriosities();
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } else {
            Timber.d("Returning curiosities from cache: %s", curiosities);
            EventBus.getDefault().post(new CuriositiesFetchedMessage(curiosities));
        }
    }

    public void addCuriosity(final @NonNull Curiosity curiosity) {
        HashSet<Curiosity> additions = new HashSet<>(1);
        additions.add(curiosity);
        addCuriosities(additions);
    }

    public void addCuriosities(final @NonNull Set<Curiosity> additions) {
        Timber.d("Adding Curiosities: %s", additions);
        for(Curiosity curiosity: additions) {
            curiosities.put(curiosity, Constants.STATUS_INITIAL);
        }

        Timber.d("Curiosities: %s", curiosities);
        StorageController.saveCuriosities(curiosities);

        for (Curiosity curiosity: additions) {
            ResultStore.getResultStore().fetchResults(curiosity);
        }
    }

    public void removeCuriosity(final @NonNull Curiosity curiosity) {
        Timber.d("Removing curiosity %s and its results from cache and storage", curiosity);

        HashMap<Curiosity, Integer> undoableCuriosity = new HashMap<>(1);
        undoableCuriosity.put(curiosity, curiosities.get(curiosity));
        curiosities.remove(curiosity);
        StorageController.saveCuriosities(curiosities);

        // Deleted curiosity only exists in memory now
        // If the app is closed/crashes, we lose the ability to undo
        undoableCuriosities.dropItem(undoableCuriosity);
    }

    public void markUnread(@NonNull Curiosity curiosity) {
        Integer setBits =
                Constants.STATUS_HAS_RESULTS |
                Constants.STATUS_HAS_UNREAD_RESULTS;

        Timber.d("Marking [%s] as unread", curiosity);
        StorageController.setStatus(curiosity, setBits);

        if (curiosities.containsKey(curiosity)) {
            Integer updatedStatus = curiosities.get(curiosity) | setBits;
            curiosities.put(curiosity, updatedStatus);
            EventBus.getDefault().post(new CuriosityChangedMessage(curiosity, updatedStatus));
        }
    }

    public void markRead(@NonNull Curiosity curiosity) {
        Integer unsetBits = Constants.STATUS_HAS_UNREAD_RESULTS;

        Timber.d("Marking [%s] as read", curiosity);
        StorageController.unsetStatus(curiosity, unsetBits);


        if (curiosities.containsKey(curiosity)) {
            Integer updatedStatus = curiosities.get(curiosity) & ~unsetBits;
            curiosities.put(curiosity, updatedStatus);
            EventBus.getDefault().post(new CuriosityChangedMessage(curiosity, updatedStatus));
        }
    }

    public void markQueryingFinished(@NonNull Curiosity curiosity) {
        Integer setBits = Constants.STATUS_QUERYING_FINISHED;

        Timber.d("Marking [%s] as unread", curiosity);
        StorageController.setStatus(curiosity, setBits);

        if (curiosities.containsKey(curiosity)) {
            Integer updatedStatus = curiosities.get(curiosity) | setBits;
            curiosities.put(curiosity, updatedStatus);
            EventBus.getDefault().post(new CuriosityChangedMessage(curiosity, updatedStatus));
        }
    }

    public void undoDeletions() {
        Set<Map<Curiosity, Integer>> savedCuriosities = undoableCuriosities.retrieveItems();

        Timber.d("Heroically Rescued Curiosities: %s", savedCuriosities);
        for (Map<Curiosity, Integer> curiosityIntegerMap: savedCuriosities) {
            curiosities.putAll(curiosityIntegerMap);
        }

        StorageController.saveCuriosities(curiosities);
    }

    @Subscribe
    public void onCuriositiesFetched(final CuriositiesFetchedMessage message) {
        Timber.d("Updating cache in Curiosities Store");
        curiosities.clear();
        curiosities.putAll(message.curiosities);
        EventBus.getDefault().unregister(this);
    }

    public Integer getStatus(Curiosity curiosity) {
        return curiosities.get(curiosity);
    }
}