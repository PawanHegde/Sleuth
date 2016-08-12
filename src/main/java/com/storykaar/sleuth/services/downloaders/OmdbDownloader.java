/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;
import com.storykaar.sleuth.services.ImageStore;
import com.storykaar.sleuth.util.ServiceGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by pawan on 6/7/16.
 */
public class OmdbDownloader implements DownloaderFactory.IDownloader {

    private static OmdbApi omdbApi = ServiceGenerator.createService("http://www.omdbapi.com/", OmdbApi.class);

    @Override
    public ResultGroup download(Curiosity curiosity) throws IOException {
        OmdbObject omdbObject;
        try {
            omdbObject = omdbApi.fetch(curiosity.query, "json").execute().body();
        } catch (IOException e) {
            throw e;
        }

        if ("False".equals(omdbObject.Response)) {
            // We did not obtain any result
            return new ResultGroup(curiosity, Source.omdbSource, new HashSet<Result>(0));
        }

        if (omdbObject.Poster != null) {
            ImageStore.getInstance().storeImage(omdbObject.Poster);
        }

        HashMap<String, String> propertiesMap = new HashMap<>();

        propertiesMap.put("Title", getInvalidAsNull(omdbObject.Title));
        propertiesMap.put("Year", getInvalidAsNull(omdbObject.Year));
        propertiesMap.put("Rated", getInvalidAsNull(omdbObject.Rated));
        propertiesMap.put("Released", getInvalidAsNull(omdbObject.Released));
        propertiesMap.put("Runtime", getInvalidAsNull(omdbObject.Runtime));
        propertiesMap.put("Genre", getInvalidAsNull(omdbObject.Genre));
        propertiesMap.put("Director", getInvalidAsNull(omdbObject.Director));
        propertiesMap.put("Writer", getInvalidAsNull(omdbObject.Writer));
        propertiesMap.put("Actors", getInvalidAsNull(omdbObject.Actors));
        propertiesMap.put("Plot", getInvalidAsNull(omdbObject.Plot));
        propertiesMap.put("Language", getInvalidAsNull(omdbObject.Language));
        propertiesMap.put("Country", getInvalidAsNull(omdbObject.Country));
        propertiesMap.put("Awards", getInvalidAsNull(omdbObject.Awards));
        propertiesMap.put("Metascore", getInvalidAsNull(omdbObject.Metascore));
        propertiesMap.put("imdbRating", getInvalidAsNull(omdbObject.imdbRating));
        propertiesMap.put("imdbVotes", getInvalidAsNull(omdbObject.imdbVotes));
        propertiesMap.put("imdbID", getInvalidAsNull(omdbObject.imdbID));
        propertiesMap.put("Type", getInvalidAsNull(omdbObject.Type));
        propertiesMap.put("Response", getInvalidAsNull(omdbObject.Response));

        Result result = new Result(propertiesMap, 800, Result.SHOW, getInvalidAsNull(omdbObject.Poster));

        Set<Result> resultSet = new HashSet<>(1);
        resultSet.add(result);

        return new ResultGroup(curiosity, Source.omdbSource, resultSet);
    }

    private String getInvalidAsNull(String value) {
        if ("N/A".equals(value)) {
            return null;
        }

        Timber.d("Setting gone: %s", value);

        return value;
    }

    interface OmdbApi {
        @GET("/")
        Call<OmdbObject> fetch(@Query("t") String title, @Query("r") String format);
    }

    public class OmdbObject {

        public String Title;
        public String Year;
        public String Rated;
        public String Released;
        public String Runtime;
        public String Genre;
        public String Director;
        public String Writer;
        public String Actors;
        public String Plot;
        public String Language;
        public String Country;
        public String Awards;
        public String Poster;
        public String Metascore;
        public String imdbRating;
        public String imdbVotes;
        public String imdbID;
        public String Type;
        public String Response;

        @Override
        public String toString() {
            return "OmdbObject{" +
                    "Title='" + Title + '\'' +
                    ", Year='" + Year + '\'' +
                    ", Rated='" + Rated + '\'' +
                    ", Released='" + Released + '\'' +
                    ", Runtime='" + Runtime + '\'' +
                    ", Genre='" + Genre + '\'' +
                    ", Director='" + Director + '\'' +
                    ", Writer='" + Writer + '\'' +
                    ", Actors='" + Actors + '\'' +
                    ", Plot='" + Plot + '\'' +
                    ", Language='" + Language + '\'' +
                    ", Country='" + Country + '\'' +
                    ", Awards='" + Awards + '\'' +
                    ", Poster='" + Poster + '\'' +
                    ", Metascore='" + Metascore + '\'' +
                    ", imdbRating='" + imdbRating + '\'' +
                    ", imdbVotes='" + imdbVotes + '\'' +
                    ", imdbID='" + imdbID + '\'' +
                    ", Type='" + Type + '\'' +
                    ", Response='" + Response + '\'' +
                    '}';
        }
    }
}
