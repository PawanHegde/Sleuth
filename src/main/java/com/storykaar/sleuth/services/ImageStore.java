/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.storykaar.sleuth.events.ImageFetchedMessage;
import com.storykaar.sleuth.services.storage.StorageController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by pawan on 17/4/16.
 */
public class ImageStore {
    private static ImageStore instance;

    private final HashMap<String, Bitmap> cacheMap = new HashMap<>();

    public static ImageStore getInstance() {
        if (instance == null) {
            synchronized (ResultStore.class) {
                if (instance == null) {
                    Timber.d("Instance of ImageStore created");
                    instance = new ImageStore();
                }
            }
        }

        return instance;
    }

    public void requestImage(final @NonNull String url) {
        String key = Integer.toHexString(url.hashCode());

        if (cacheMap.containsKey(key)) {
            Timber.v("Found a mention of %s in cache", url);
            EventBus.getDefault().post(new ImageFetchedMessage(url, cacheMap.get(key)));
        } else {
            StorageController.requestImage(url);
        }
    }

    @Subscribe
    public void onImageRetrieved(final ImageFetchedMessage message) {
        if (message == null) {
            fetchImage(message.url);
        }
    }

    private void fetchImage(final @NonNull String url) {

    }

    public void storeImage(final @NonNull String url) {

    }
}