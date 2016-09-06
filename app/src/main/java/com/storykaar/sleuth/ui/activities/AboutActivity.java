/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.storykaar.sleuth.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About");
        }
    }
}
