/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;

import java.io.Serializable;

import timber.log.Timber;

import static com.storykaar.sleuth.services.downloaders.DownloaderFactory.IDownloader;

/**
 * Created by pawan on 31/3/16.
 */
public class CallRestJob extends Job implements Serializable {
    private final Curiosity curiosity;
    private final Source source;

    private ResultGroup results;

    public CallRestJob(final Params params,
                       final Curiosity curiosity,
                       final Source source) {
        super(params.persist());

        DownloaderFactory.IDownloader downloader = DownloaderFactory.getDownloader(source);
        this.curiosity = curiosity;
        this.source = source;

        Timber.v("Created an instance of CallRestJob with downloader %s and curiosity %s",
                downloader, curiosity);
    }

    public Curiosity getCuriosity() {
        return curiosity;
    }

    public Source getSource() {
        return source;
    }

    public ResultGroup getResults() {
        return results;
    }

    @Override
    public void onAdded() {
        Timber.v("New Job added: %s", this);
    }

    @Override
    public void onRun() throws Throwable {
        IDownloader downloader = DownloaderFactory.getDownloader(source);

        results = downloader.download(curiosity);
        Timber.v("Results obtained: %s", results);
    }

    @Override
    protected void onCancel(int cancelReason) {
        Timber.w("Job cancelled: %d", cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        Timber.e(throwable, "Job failed at runcount %d out of max %d with error %s", runCount, maxRunCount);
        RetryConstraint constraint = new RetryConstraint(true);
        constraint.setNewDelayInMs(20000 *
                (long) Math.pow(2, Math.max(0, runCount - 1)));
        return constraint;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CallRestJob)) {
            return false;
        }

        CallRestJob other = (CallRestJob) object;

        Timber.v("Equals method called for %s", object);
        return this.source.equals(other.source)
                && this.curiosity.equals(other.curiosity);

    }

    @Override
    public String toString() {
        return "CallRestJob{" +
                "curiosity=" + curiosity +
                ", source='" + source;
    }
}
