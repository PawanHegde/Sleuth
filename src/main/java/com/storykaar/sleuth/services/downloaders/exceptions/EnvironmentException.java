/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by pawan on 25/3/16.
 */
public class EnvironmentException extends Exception {
    public EnvironmentException(@NonNull String description) {
        super(description);
    }
}
