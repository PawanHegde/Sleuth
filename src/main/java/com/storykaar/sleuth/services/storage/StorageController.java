/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.storage;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.storykaar.sleuth.events.CuriositiesFetchedMessage;
import com.storykaar.sleuth.events.ImageFetchedMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by pawan on 24/4/16.
 *
 * Generic controller to handle storage
 */
public class StorageController {
    public static String CURIOSITIES_FILE = "curiosities.txt";
    public static String SOURCES_FILE = "sources.txt";

    private static FileStorage storage = new FileStorage();


    public static void persist(final @NonNull Curiosity curiosity) {
        //TODO: Persist a single curiosity
        throw new NoSuchMethodError();
    }

    public static void deleteCuriosity(final @NonNull Curiosity curiosity) {
        //TODO: Delete Curiosity and all the relevant results
        throw new NoSuchMethodError();
    }

    public static void requestCuriosities() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                Map<Curiosity, Integer> curiosities = storage.requestCuriosities(CURIOSITIES_FILE);
                Timber.i("Returning Curiosities: " + curiosities);
                EventBus.getDefault().post(new CuriositiesFetchedMessage(curiosities));
            }
        });
    }

    public static void saveCuriosities(final @NonNull Map<Curiosity, Integer> curiosities) {
        Timber.i("Saving Curiosities");
        AsyncTask.execute(new Runnable() {
            public void run() {
                storage.requestSaveCuriosities(curiosities);
            }
        });
    }

//    public static void setStatus(@NonNull final Curiosity curiosity, @NonNull final Integer status) {
//        Timber.i("Saving status of %s as %s", curiosity, status);
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                storage.requestSaveStatus(curiosity, status);
//            }
//        });
//    }

    /*
     * Request the retrieval of results for the provided curiosity
     * from storage
     */
    public static void requestRetrieval(@NonNull final Curiosity curiosity) {
        Timber.i("Requesting results for curiosity %s ", curiosity);
        AsyncTask.execute(new Runnable () {
            @Override
            public void run() {
                Set<ResultGroup> results = new HashSet<>();
                Set<Source> requestedSources = new HashSet<>();

                try {
                    results = storage.requestRetrieval(curiosity);
                    requestedSources = requestSources(curiosity);
                    Timber.d("Requested Sources for %s are %s", curiosity, requestedSources);
                } catch (IOException e) {
                    Timber.e("Failed to retrieve results from storage! This is a really bad failure");
                }

                EventBus.getDefault().post(new ResultsRetrievedMessage(curiosity, results, requestedSources));
            }
        });
    }

    public static void requestStorage(final @NonNull Curiosity curiosity, @NonNull final ResultGroup resultGroup) {
        Timber.i("Requesting storage for curiositiy: " + curiosity);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    storage.requestStorage(curiosity, resultGroup);
                } catch (IOException e) {
                    Timber.e("Failed to store the result. This is a really bad failure");
                }
            }
        });
    }

    public static void requestDeletion(final @NonNull Curiosity curiosity) {
        Timber.i("Requesting deletion of results for curiositiy: %s", curiosity);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                 storage.requestDeletion(curiosity);
            }
        });
    }

    public static void requestImage(final @NonNull String url) {
        Timber.i("Requesting image for url %s ", url);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap image = null;
                try {
                    image = storage.requestImage(url);
                } catch (IOException e) {
                    Timber.e("Failed to retrieve the image from storage. This is a really bad failure");
                }
                // Image could be null. Deal with it.
                EventBus.getDefault().post(new ImageFetchedMessage(url, image));
            }
        });
    }

    public static void requestSaveSources(final @NonNull Curiosity curiosity, final @NonNull Set<Source> requestedSources) {
        Timber.d("Saving the list of sources that will be contacted for %s as %s", curiosity, requestedSources);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    storage.saveSourcesForCuriosity(curiosity, requestedSources);
                } catch (IOException e) {
                    Timber.e("Failure while trying to save the list of sources for the given curiosity");
                }
            }
        });
    }

    public static Set<Source> requestSources(final @NonNull Curiosity curiosity) {
        Timber.d("Returning the list of sources that will be contacted for %s", curiosity);
        return storage.requestSourcesForCuriosity(curiosity);
    }

    public static void vacuum() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                storage.vacuum();
            }
        });
    }

    public static void setStatus(@NonNull final Curiosity curiosity, @NonNull final Integer status) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Map<Curiosity, Integer> curiosityIntegerMap = storage.requestCuriosities(CURIOSITIES_FILE);
                if (curiosityIntegerMap.containsKey(curiosity)) {
                    Integer currentStatus = curiosityIntegerMap.get(curiosity);
                    curiosityIntegerMap.put(curiosity, currentStatus | status);
                    saveCuriosities(curiosityIntegerMap);
                }
            }
        });
    }

    public static void unsetStatus(@NonNull final Curiosity curiosity, @NonNull final Integer status) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Map<Curiosity, Integer> curiosityIntegerMap = storage.requestCuriosities(CURIOSITIES_FILE);
                if (curiosityIntegerMap.containsKey(curiosity)) {
                    Integer currentStatus = curiosityIntegerMap.get(curiosity);
                    curiosityIntegerMap.put(curiosity, currentStatus & ~status);
                    saveCuriosities(curiosityIntegerMap);
                }
            }
        });
    }

    public static class ResultsRetrievedMessage {
        final public Curiosity curiosity;
        final public Set<ResultGroup> resultGroupSet;
        final public Set<Source> requestedSources;

        public ResultsRetrievedMessage(Curiosity curiosity, Set<ResultGroup> resultGroupSet, Set<Source> requestedSources) {
            this.curiosity = curiosity;
            this.resultGroupSet = resultGroupSet;
            this.requestedSources = requestedSources;
        }
    }
}