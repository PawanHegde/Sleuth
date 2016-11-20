/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.CancelResult;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.TagConstraint;
import com.birbit.android.jobqueue.callback.JobManagerCallbackAdapter;
import com.storykaar.sleuth.SleuthApp;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;
import com.storykaar.sleuth.services.ImageStore;
import com.storykaar.sleuth.services.ResultStore;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by pawan on 2/6/16.
 * Manages the logic of download
 * and the order of download
 */
public class DownloadManager {

    public static Set<Source> manage(@NonNull final Curiosity curiosity) {
        Timber.d("Managing started");
        Set<Source> requestedSources = new HashSet<>();

        JobManager jobManager = SleuthApp.getJobManager();

        String downloadMode = PreferenceManager.getDefaultSharedPreferences(SleuthApp.getAppContext())
                .getString("mobile_data_use", "wifi");

        Timber.d("Download mode is set to %s", downloadMode);

        Boolean wifiOnly = downloadMode.equals("wifi");

        Params params = new Params(1)
                .setRequiresNetwork(true)
                .setRequiresUnmeteredNetwork(wifiOnly)
                .addTags(curiosity.toString());

        Job job = new CallRestJob(params, curiosity, Source.duckDuckGo);
        jobManager.addJobInBackground(job);
        Timber.v("Added job %s", job);
//        jobManager.addCallback(new CallBackHandler(curiosity, Source.duckDuckGo));
        requestedSources.add(Source.duckDuckGo);

        job = new CallRestJob(params, curiosity, Source.wordnick);
        jobManager.addJobInBackground(job);
        Timber.v("Added job %s", job);
//        jobManager.addCallback(new CallBackHandler(curiosity, Source.wordnick));
        requestedSources.add(Source.wordnick);

        job = new CallRestJob(params, curiosity, Source.omdbSource);
        jobManager.addJobInBackground(job);
        Timber.v("Added job %s", job);
//        jobManager.addCallback(new CallBackHandler(curiosity, Source.omdbSource));
        requestedSources.add(Source.omdbSource);

        Timber.d("Managing finished");
        return requestedSources;
    }

    public static void manage(@NonNull Curiosity curiosity, @NonNull String url) {
        JobManager jobManager = SleuthApp.getJobManager();

        String downloadMode = PreferenceManager.getDefaultSharedPreferences(SleuthApp.getAppContext())
                .getString("mobile_data_use", "wifi");

        Boolean wifiOnly = downloadMode.equals("wifi") || downloadMode.equals("balanced");

        Params params = new Params(1)
                .setRequiresNetwork(true)
                .setRequiresUnmeteredNetwork(wifiOnly)
                .addTags(curiosity.toString());

        Job job = new ImageDownloadJob(params, curiosity, url);
        jobManager.addJobInBackground(job);
    }

    public static void cancel(@NonNull final Curiosity curiosity) {

        SleuthApp.getJobManager().cancelJobsInBackground(new CancelResult.AsyncCancelCallback() {
            @Override
            public void onCancelled(CancelResult cancelResult) {
                Timber.d("Cancelled downloads for %s", curiosity);
            }
        }, TagConstraint.ANY, curiosity.toString());
    }

    public static class CallBackHandler extends JobManagerCallbackAdapter {
//        Curiosity curiosity;
//        Source source;
//
//        CallBackHandler(Curiosity curiosity, Source source) {
//            this.curiosity = curiosity;
//            this.source = source;
//        }

        @Override
        public void onJobAdded(Job job) {
            Timber.v("Job %s added to the queue with handler %s", job, this);
        }

        @Override
        public void onJobRun(Job job, int resultCode) {
            Timber.v("Ran job %s with result %s", job, resultCode);
        }

        @Override
        public void onJobCancelled(Job job, boolean byCancelRequest) {
            Timber.v("Job %s has been cancelled. Whether it was by request is %s.", job, byCancelRequest);
        }

        @Override
        public void onDone(@NonNull Job genericJob) {

            if (!(genericJob instanceof CallRestJob)) {
                Timber.w("ResultStore got a callback for a non-CallRestJob job");
                return;
            }

            CallRestJob job = (CallRestJob) genericJob;

//            if (!curiosity.equals(job.getCuriosity()) || !source.equals(job.getSource())) {
//                Timber.d("Looking for %s & %s. Found %s & %s", curiosity, source, job.getCuriosity(), job.getSource());
//                return;
//            }

//            SleuthApp.getJobManager().removeCallback(this);

            if (job.isCancelled()) {
                Timber.w("Job %s got cancelled", job);
                return;
            }

            //Timber.v("%s code returned by %s", resultCode, job);

            ResultGroup results = job.getResults();

            Timber.i("Downloading completed for %s", job);
            ResultStore.getResultStore().addResults(results);
        }
    }

    public static class ImageJobCallbackHandler extends JobManagerCallbackAdapter {
        @Override
        public void onJobAdded(Job job) {
            Timber.v("Job %s added to the queue with handler %s", job, this);
        }

        @Override
        public void onJobRun(Job job, int resultCode) {
            Timber.v("Ran job %s with result %s", job, resultCode);
        }

        @Override
        public void onJobCancelled(Job job, boolean byCancelRequest) {
            Timber.v("Job %s has been cancelled. Whether it was by request is %s.", job, byCancelRequest);
        }

        @Override
        public void onDone(@NonNull Job genericJob) {

            if (!(genericJob instanceof ImageDownloadJob)) {
                Timber.w("ResultStore got a callback for a non-ImageDownloadJob job");
                return;
            }

            ImageDownloadJob job = (ImageDownloadJob) genericJob;

            if (job.isCancelled()) {
                Timber.w("Job %s got cancelled", job);
                return;
            }

            Timber.i("Downloading completed for %s. We have %s", job, job.getImage());
            ImageStore.getInstance().storeImage(job.getUrl(), job.getImage());
        }
    }
}