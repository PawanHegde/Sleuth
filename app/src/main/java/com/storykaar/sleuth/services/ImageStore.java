/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.storykaar.sleuth.events.ImageFetchedMessage;
import com.storykaar.sleuth.services.storage.StorageController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by pawan on 17/4/16.
 *
 * The centralised way to access all the images downloaded by this app
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
                    EventBus.getDefault().register(instance);
                }
            }
        }

        return instance;
    }

    public void requestImage(@NonNull final String url) {
        if (!url.isEmpty()) {
            final String key = Integer.toHexString(url.hashCode());

            if (cacheMap.containsKey(key)) {
                Timber.v("Found a mention of %s in cache", url);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Boolean hasImage = cacheMap.get(key) != null;
                        EventBus.getDefault().post(new ImageFetchedMessage(url, cacheMap.get(key), hasImage));
                    }
                });
            } else {
                StorageController.requestImage(url);
            }
        }
    }

    @Subscribe
    public void onImageRetrieved(@NonNull StorageController.ImageFetchedMessage message) {
        /* Image here could be *null*. We don't care
        ** We simply store the null value in the cache so that the next time we get queried,
        ** we can simply tell them that the image does not exist (in the requestImage function)
        ** Instead of trying to fetch it again from the storage or even worse, the internet.
         */
        cacheMap.put(Integer.toHexString(message.url.hashCode()), message.image);
        Timber.d("Cached the image from %s in storage to cache", message.url);
    }

    public void storeImage(@NonNull String url, @NonNull Bitmap image) {
        cacheMap.put(Integer.toHexString(url.hashCode()), image);
        Timber.d("Cached the image from %s to cache", url);

        StorageController.requestSaveImage(url, image);
    }
}