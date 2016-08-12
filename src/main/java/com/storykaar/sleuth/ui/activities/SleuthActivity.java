/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.storykaar.sleuth.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SleuthActivity
    extends AppCompatActivity{

    @BindView(R.id.action_bar)
    Toolbar actionBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle toggle;

    public SleuthActivity() {
        super();
        System.out.println("Activity object constructed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("SUPERONCREATEFINISHED");
        setContentView(R.layout.activity_sleuth);
        ButterKnife.bind(this);
        setSupportActionBar(actionBar);
        if (getSupportActionBar() == null) {
            Timber.e("Failed to get an action bar. Killing self with a Samurai sword");
            throw new IllegalStateException("Unable to find actionbar");
        }
        System.out.println("ACTIVITYCREATEFINISHED!");
    }

    @Override
    protected void onStart() {
        System.out.println("ACTIVITYSTARTSTARTED");
        super.onStart();
        System.out.println("SUPERACTIVITYONSTARTFINISHED");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, actionBar, R.string.open_drawer, R.string.close_drawer);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_item_settings:
                        Intent settingsIntent = new Intent(SleuthActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case R.id.navigation_item_about:
                        Intent aboutIntent  = new Intent(SleuthActivity.this, AboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                }

                drawerLayout.closeDrawers();

                return true;
            }
        });
        System.out.println("ADDINGDRAWERLISTENERFINISHED");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        toggle.onConfigurationChanged(newConfig);
    }
}