/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.storykaar.sleuth.events.ResultsFetchedMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;
import com.storykaar.sleuth.services.downloaders.DownloadManager;
import com.storykaar.sleuth.services.storage.StorageController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 * Central hub of Results' backend
 */
public class ResultStore {
    private static ResultStore instance;
    private final HashMap<Curiosity, TreeSet<ResultGroup>> resultMap = new HashMap<>();
    private final HashMap<Curiosity, Set<Source>> requestedSourcesMap = new HashMap<>();
    private final Set<Curiosity> undoableCuriosities = new HashSet<>(1);

    private static final Integer DELETE_LEEWAY = 3000;
    private Handler deleteHandler = new Handler();
    private Runnable deleteTask = new Runnable() {
        @Override
        public void run() {
            for (Curiosity curiosity: undoableCuriosities) {
                //DownloadManager.cancel(curiosity);
                StorageController.requestDeletion(curiosity);
            }
        }
    };

    private ResultStore() {
        EventBus.getDefault().register(this);
    }

    public static ResultStore getResultStore() {
        Timber.d("Instance of ResultStore requested");
        if (instance == null) {
            synchronized (ResultStore.class) {
                if (instance == null) {
                    instance = new ResultStore();
                }
            }
        }

        return instance;
    }

    private void cacheResults(@NonNull final Curiosity curiosity, @NonNull final ResultGroup results) {
        if (!resultMap.containsKey(curiosity)) {
            synchronized (this) {
                if (!resultMap.containsKey(curiosity)) {
                    Timber.d("Added a set for the results of %s", curiosity);
                    resultMap.put(curiosity, new TreeSet<ResultGroup>());
                }
            }
        }

        resultMap.get(curiosity).add(results);

        Timber.i("Cached new results for %s from %s", curiosity, results.getSource());
    }

    // Results are requested from the cache
    public void requestResults(final @NonNull Curiosity curiosity, final @NonNull Boolean refresh) {
        if (refresh) {
            Timber.d("Disregarding the caches and fetching from the internet for %s", curiosity);
            fetchResults(curiosity);
            return;
        }

        // TODO: Mark the cache as unclean if refresh is checked
        if (resultMap.containsKey(curiosity)) {
            Timber.v("Found a mention of %s in cache", curiosity);
            // The returned value *could* be an empty set
            EventBus.getDefault().post(new ResultsFetchedMessage(curiosity, resultMap.get(curiosity)));
        } else {
            StorageController.requestRetrieval(curiosity);
        }
    }

    // Failing the cache, the database is queried for results
    // We also get the status of the download jobs
    // We never initiate a download task during retrieval
    // Unless the user decides to force refresh
    @Subscribe
    public void onResultsRetrieved(final @NonNull StorageController.ResultsRetrievedMessage message) {
        Timber.d("Database query returned from the database for %s", message.curiosity);

        if (message.resultGroupSet.isEmpty()) {
            Timber.i("The database remembers not finding anything about this on the internet. " +
                    "Will store an empty resultset");
        }

        requestedSourcesMap.put(message.curiosity, message.requestedSources);

        for (ResultGroup resultGroup : message.resultGroupSet) {
            cacheResults(message.curiosity, resultGroup);
        }

        EventBus.getDefault().post(new ResultsFetchedMessage(message.curiosity, message.resultGroupSet));
    }

    // The knowledge of the internets is summoned
    public void fetchResults(final Curiosity curiosity) {
        Set<Source> requestedSources = DownloadManager.manage(curiosity);
        StorageController.requestSaveSources(curiosity, requestedSources);
    }

    // The wizards of the internet return with search results
    public void addResults(final ResultGroup results) {
        Curiosity curiosity = results.getCuriosity();

        if (results.getResults().isEmpty()) {
            Timber.w(results.getSource() + " returned no results for " + results.getCuriosity() + ". We will store an empty file");
        } else {
            CuriosityStore.getInstance().markUnread(curiosity);
        }

        StorageController.requestStorage(curiosity, results);

        cacheResults(curiosity, results);

        Set<ResultGroup> resultGroups = resultMap.get(curiosity);

        Set<Source> availableSources;
        if (resultGroups != null) {
            availableSources = new HashSet<>(resultGroups.size());
            for (ResultGroup resultGroup: resultGroups) {
                availableSources.add(resultGroup.getSource());
            }
        } else {
            availableSources = new HashSet<>(0);
        }

        Timber.v("Added results for %s from %s", curiosity, results.getSource());

        if (requestedSourcesMap.get(curiosity) == null) {
            requestedSourcesMap.put(curiosity, StorageController.requestSources(curiosity));
        }

        Set<Source> requestedSources = requestedSourcesMap.get(curiosity);
        Timber.d("Contains results from sources: %s\nExpected from: %s", availableSources, requestedSources);

        Boolean queryingFinished = availableSources.containsAll(requestedSources);

        if (queryingFinished) {
            CuriosityStore.getInstance().markQueryingFinished(curiosity);
        }

        EventBus.getDefault().post(new ResultsFetchedMessage(curiosity, resultMap.get(curiosity)));
    }

    public void deleteResultsFor(final Curiosity curiosity) {
        Timber.d("Delete requested for %s", curiosity);

        resultMap.remove(curiosity);

//        undoableCuriosities.add(curiosity);
//        // TODO: Delete if app closes before the deletions take place
//        deleteHandler.removeCallbacks(deleteTask);
//        deleteHandler.postDelayed(deleteTask, DELETE_LEEWAY);
    }

    public void undoDeletions() {
        deleteHandler.removeCallbacks(deleteTask);
    }

    public void vacuum() {
        Timber.d("Vacuuming");
        StorageController.vacuum();
    }
}