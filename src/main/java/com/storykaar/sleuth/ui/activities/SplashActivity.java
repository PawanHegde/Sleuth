/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import timber.log.Timber;

/**
 * Created by pawan on 21/7/16.
 *
 * Splashscreen
 */
public class SplashActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("Splashin' and Paddlin'");
        Intent intent = new Intent(this, SleuthActivity.class);
        startActivity(intent);
        finish();
    }
}
