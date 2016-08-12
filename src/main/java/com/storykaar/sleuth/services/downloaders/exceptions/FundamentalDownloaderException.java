/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by pawan on 25/3/16.
 */
public class FundamentalDownloaderException extends Exception {
    public FundamentalDownloaderException(@NonNull String description) {
        super(description);
    }
}
