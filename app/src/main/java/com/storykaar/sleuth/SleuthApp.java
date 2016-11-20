/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.storykaar.sleuth.services.JobSchedulerService;
import com.storykaar.sleuth.services.JobService;
import com.storykaar.sleuth.services.ResultStore;
import com.storykaar.sleuth.services.downloaders.DownloadManager;

import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 *
 * Initialisation of stuff required by the whole app
 */
public class SleuthApp extends Application {
    private static SleuthApp instance = null;

    private static JobManager jobManager;

    private static void configureJobManager(@NonNull Context context) {
        Configuration.Builder builder = new Configuration.Builder(context)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(1)//3 jobs per consumer
                .consumerKeepAlive(120);//wait 2 minute

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(instance,
                    JobSchedulerService.class), true);
        } else {
            int enableGcm = GoogleApiAvailability.getInstance().
                    isGooglePlayServicesAvailable(instance);
            if (enableGcm == ConnectionResult.SUCCESS) {
                builder.scheduler(GcmJobSchedulerService.createSchedulerFor(instance,
                        JobService.class), true);
            }
        }

        jobManager = new JobManager(builder.build());

        jobManager.addCallback(new DownloadManager.CallBackHandler());
        jobManager.addCallback(new DownloadManager.ImageJobCallbackHandler());
    }

    public static Context getAppContext() {
        checkInstance();
        return instance;
    }

    private static void checkInstance() {
        if (instance == null) {
            throw new IllegalStateException("Application Instance not initialised!");
        }
    }

    public static JobManager getJobManager() {
        if (jobManager == null) {
            checkInstance();
            configureJobManager(instance);
        }
        return jobManager;
    }

    @Override
    public void onCreate() {
        System.out.println("APPCREATESTARTED");
        super.onCreate();

        instance = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(Timber.tag("Sleuthag"));
        }

        System.out.println("APPCREATEFINISHED");

        ResultStore.getResultStore().vacuum();
        SleuthApp.configureJobManager(instance);
    }
}
