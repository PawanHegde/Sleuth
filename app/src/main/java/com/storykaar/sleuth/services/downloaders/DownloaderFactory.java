/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 */
public class DownloaderFactory {
    private static WordnickDownloader wordnickDownloader;
    private static DuckDuckGoDownloader duckDuckGoDownloader;
    private static OmdbDownloader omdbDownloader;

    public interface IDownloader {
        ResultGroup download(Curiosity curiosity) throws IOException;
    }

    public static IDownloader getDownloader(Source source) {
        Timber.v("Getting Downloader for %s", source);

        if (Source.duckDuckGo.equals(source)) {
            if (duckDuckGoDownloader == null) {
                duckDuckGoDownloader = new DuckDuckGoDownloader();
            }
            return duckDuckGoDownloader;
        } else if (Source.wordnick.equals(source)) {
            if (wordnickDownloader == null) {
                wordnickDownloader = new WordnickDownloader();
            }
            return wordnickDownloader;
        } else if (Source.omdbSource.equals(source)) {
            if (omdbDownloader == null) {
                omdbDownloader = new OmdbDownloader();
            }
            return omdbDownloader;
        }

        Timber.e("Invalid source downloader requested: %s", source);
        throw new RuntimeException("Invalid source downloader");
    }
}
