/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.events;

import android.graphics.Bitmap;

/**
 * Created by pawan on 9/7/16.
 *
 * Message with the downloaded image
 */
public class ImageFetchedMessage {
    final public String url;
    final public Bitmap image;
    final public Boolean hasImage;

    public ImageFetchedMessage(String url, Bitmap image, Boolean hasImage) {
        this.url = url;
        this.image = image;
        this.hasImage = hasImage;
    }
}
