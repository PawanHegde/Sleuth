/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.storykaar.sleuth.model.Curiosity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by pawan on 9/9/16.
 * <p>
 * Download images from url
 */
public class ImageDownloadJob extends Job implements Serializable {
    private final Curiosity curiosity;
    private final String url;

    private Bitmap image;

    public ImageDownloadJob(final Params params,
                            final Curiosity curiosity,
                            final String url) {
        super(params.persist());

        this.curiosity = curiosity;
        this.url = url;

        Timber.v("Created an instance of ImageDownloadJob for %s", url);
    }

    public Curiosity getCuriosity() {
        return curiosity;
    }

    public String getUrl() {
        return url;
    }

    public Bitmap getImage() {
        return image;
    }

    @Override
    public void onAdded() {
        Timber.v("New Job added: %s", this);
    }

    @Override
    public void onRun() throws Throwable {
        image = download(url);
        Timber.v("Image obtained for %s", url);
    }

    @Override
    protected void onCancel(int cancelReason) {
        Timber.w("Job cancelled: %d", cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        Timber.e(throwable, "Image download Job failed at runcount %d out of max %d with error %s", runCount, maxRunCount);
        RetryConstraint constraint = new RetryConstraint(true);
        constraint.setNewDelayInMs(20000 *
                (long) Math.pow(2, Math.max(0, runCount - 1)));
        return constraint;
    }

    private Bitmap download(String urlString) throws IOException {
        try {
            Timber.d("In ImageDownloadJob url was %s", urlString);
            URL url = new URL(urlString);
            Bitmap image;

            try (InputStream inputStream = (InputStream) url.getContent()) {
                image = BitmapFactory.decodeStream(inputStream);
            }

            Timber.d("Downloaded from the url %s, the image %s", url, image);
            return image;
        } catch (Exception e) {
            Timber.e(e, "Caught exception in Imagedownload job");
        }

        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageDownloadJob)) {
            return false;
        }

        ImageDownloadJob other = (ImageDownloadJob) object;

        Timber.v("Equals method called for %s", object);
        return this.url.equals(other.url)
                && this.curiosity.equals(other.curiosity);

    }

    @Override
    public String toString() {
        return "ImageDownloadJob{" +
                "curiosity=" + curiosity +
                ", url='" + url;
    }
}
