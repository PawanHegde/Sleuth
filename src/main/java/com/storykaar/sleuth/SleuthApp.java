/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.storykaar.sleuth.services.JobService;
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
    }

    private static void configureJobManager(@NonNull Context context) {
        Configuration configuration = new Configuration.Builder(context)
                /*.customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";
                    @Override
                    public boolean isDebugEnabled() {
                        return false;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })*/
                .scheduler(GcmJobSchedulerService.createSchedulerFor(context, JobService.class), false)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(1)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        jobManager = new JobManager(configuration);

        jobManager.addCallback(new DownloadManager.CallBackHandler());
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
}
