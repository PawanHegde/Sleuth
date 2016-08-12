/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

import android.graphics.Bitmap;

/**
 * Created by pawan on 9/7/16.
 */
public class ImageFetchedMessage {
    final public String url;
    final public Bitmap image;

    public ImageFetchedMessage(String url, Bitmap image) {
        this.url = url;
        this.image = image;
    }
}
