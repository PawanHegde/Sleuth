/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.storage;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.storykaar.sleuth.SleuthApp;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;
import com.storykaar.sleuth.util.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by pawan on 4/6/16.
 *
 * Concrete implementation used for storage to files directly (as opposed to the database)
 */
public class FileStorage {

    private static final String IMAGE_FOLDER = "images";

    private static String getCuriosityCode(final @NonNull Curiosity curiosity) {
        return Integer.toHexString(curiosity.hashCode());
    }

    public synchronized Map<Curiosity, Integer> requestCuriosities(final @NonNull String curiositiesFile) {
        Map<Curiosity, Integer> curiosities = new HashMap<>();
        try {
            Type typeOfT = new TypeToken<Set<Curiosity>>(){}.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(typeOfT, new CuriosityMapAdapter()).create();

            curiosities = gson.fromJson(
                    FileUtil.byteArrayToString(
                            FileUtil.readFromStream(
                                    new File(SleuthApp.getAppContext().getFilesDir(),
                                            curiositiesFile))),
                    typeOfT);
        } catch (JsonSyntaxException | FileNotFoundException e) {
            if (e instanceof JsonSyntaxException) {
                Timber.e(e, "The Curiosities file doesn't have proper Json syntax");

                try {
                    String curiosityFileValue = FileUtil.byteArrayToString(
                            FileUtil.readFromStream(
                                    new File(SleuthApp.getAppContext().getFilesDir(),
                                            curiositiesFile)));
                    Timber.d("Got the value %s", curiosityFileValue);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            Timber.e("Did not find a file to store curiosities. Will try to create a new one");

            try {
                new File(SleuthApp.getAppContext().getFilesDir(), curiositiesFile).createNewFile();
            } catch (IOException e1) {
                Timber.e("Failed to create a new file to store curiosities. This is catastrophic!");
            }
        } catch (IOException e) {
            Timber.e("Failed to read the curiosities file! This is embarrassing");
        } finally {
            if (curiosities == null) {
                curiosities = new HashMap<>();
            }
        }

        return curiosities;
    }

    public synchronized void requestSaveCuriosities(final @NonNull Map<Curiosity, Integer> curiosities) {
        Type typeOfT = new TypeToken<HashMap<Curiosity, Integer>>(){}.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(typeOfT, new CuriosityMapAdapter()).create();

        try {
            FileUtil.writeToFile(
                    new File(
                            SleuthApp.getAppContext().getFilesDir(),
                            StorageController.CURIOSITIES_FILE),
                    gson.toJson(curiosities, typeOfT));
        } catch (IOException e) {
            Timber.e("Failed to write to the curiosities file! This is super embarrassing");
        }
    }

    public synchronized Set<ResultGroup> requestRetrieval(final @NonNull Curiosity curiosity) throws IOException {
        Gson gson = new Gson();
        String folderName = getCuriosityCode(curiosity);
        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);

        if (!folder.exists() || !folder.isDirectory()) {
            return new HashSet<>(0);
        }

        Set<ResultGroup> resultGroupSet = new HashSet<>();
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !(s.equals(StorageController.SOURCES_FILE));
            }
        });

        for (File file: files) {
            ResultGroup resultGroup = gson.fromJson(FileUtil.byteArrayToString(FileUtil.readFromStream(file)), ResultGroup.class);
            resultGroupSet.add(resultGroup);
        }

        return resultGroupSet;
    }

    public synchronized void requestStorage(final @NonNull Curiosity curiosity, final @NonNull ResultGroup resultGroup) throws IOException {
        Gson gson = new Gson();
        String folderName = getCuriosityCode(curiosity);
        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, resultGroup.getSource().getName());
        if (!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException("Failed to create a file to write the results of " + curiosity
                        + " from " + resultGroup.getSource());
            }
            file.setWritable(true);
        }

        FileUtil.writeToFile(file, gson.toJson(resultGroup, ResultGroup.class));
    }

    public synchronized void requestDeletion(final @NonNull Curiosity curiosity) {
        String folderName = getCuriosityCode(curiosity);
        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);
        FileUtil.deleteFileRecursively(folder);
    }

    public synchronized Bitmap requestImage(@NonNull String url) throws IOException {
        String fileName = Integer.toHexString(url.hashCode());

        File imageFile = new File(SleuthApp.getAppContext().getFilesDir() + "/" + IMAGE_FOLDER, fileName);

        return FileUtil.byteArrayToBitmap(FileUtil.readFromStream(imageFile));
    }

    public synchronized void requestSaveImage(@NonNull String url, @NonNull Bitmap bitmap) throws IOException {
        String fileName = Integer.toHexString(url.hashCode());

        File imageFolder = new File(SleuthApp.getAppContext().getFilesDir() + "/" + IMAGE_FOLDER);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        File imageFile = new File(SleuthApp.getAppContext().getFilesDir() + "/" + IMAGE_FOLDER, fileName);

        if (!imageFile.exists()) {
            if (!imageFile.createNewFile()) {
                throw new IOException("Failed to create a file to store the image from %s" + url);
            }
        }

        FileUtil.writeToFile(imageFile, bitmap);
    }

    public synchronized void saveSourcesForCuriosity(final @NonNull Curiosity curiosity, final @NonNull Set<Source> requestedSources) throws IOException {
        Gson gson = new Gson();
        String folderName = getCuriosityCode(curiosity);
        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, StorageController.SOURCES_FILE);

        FileUtil.writeToFile(file, gson.toJson(requestedSources));
    }

    public synchronized Set<Source> requestSourcesForCuriosity(final @NonNull Curiosity curiosity) {
        Gson gson = new Gson();
        String folderName = getCuriosityCode(curiosity);
        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);

        if (!folder.exists() || !folder.isDirectory()) {
            Timber.i("No directory found for %s", curiosity);
            return new HashSet<>(0);
        }

        File sourcesFile = new File(folder, StorageController.SOURCES_FILE);

        Set<Source> sources;
        try {
            Type type = new TypeToken<Set<Source>>(){}.getType();
            sources = gson.fromJson(FileUtil.byteArrayToString(FileUtil.readFromStream(sourcesFile)), type);
        } catch (IOException e) {
            Timber.e("Failed to read the list of sources for curiosity %s", curiosity);
            return new HashSet<>(0);
        }

        return sources;
    }

    public void vacuum() {
        Map<Curiosity, Integer> curiosityIntegerMap = requestCuriosities(StorageController.CURIOSITIES_FILE);
        Set<String> validFolderNames = new HashSet<>(curiosityIntegerMap.size());

        for (Curiosity curiosity: curiosityIntegerMap.keySet()) {
            validFolderNames.add(getCuriosityCode(curiosity));
        }

        validFolderNames.add(IMAGE_FOLDER);

        Timber.d("Valid folder names are: %s", validFolderNames);

        File rootFolder = SleuthApp.getAppContext().getFilesDir();
        File[] dataFolders = rootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File dataFolder: dataFolders) {
            if(!validFolderNames.contains(dataFolder.getName())) {
                boolean success = FileUtil.deleteFileRecursively(dataFolder);
                Timber.d("Deleted the folder %s: %s", dataFolder.getName(), success);
            }
        }
    }

//    public void requestSaveStatus(Curiosity curiosity, Integer status) {
//        Gson gson = new Gson();
//        String folderName = getCuriosityCode(curiosity);
//        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);
//
//        if (!folder.exists()) {
//            folder.mkdirs();
//        }
//
//        File file = new File(folder, StorageController.STATUS_FILE);
//
//        try {
//            FileUtil.writeToFile(file, gson.toJson(status));
//        } catch (IOException e) {
//            Timber.e(e, "Failed to save the status for %s." +
//                    " We will lose the ability to show the correct status unless corrected", curiosity);
//        }
//    }
//
//    public Integer requestStatus(Curiosity curiosity) {
//        Gson gson = new Gson();
//        String folderName = getCuriosityCode(curiosity);
//        File folder = new File(SleuthApp.getAppContext().getFilesDir(), folderName);
//
//        if (!folder.exists() || !folder.isDirectory()) {
//            Timber.i("No directory found for %s", curiosity);
//            return 0;
//        }
//
//        File statusFile = new File(folder, StorageController.STATUS_FILE);
//
//        Integer status;
//        try {
//            status = gson.fromJson(FileUtil.byteArrayToString(FileUtil.readFromStream(statusFile)), Integer.class);
//        } catch (IOException e) {
//            Timber.e("Failed to read the status for curiosity %s", curiosity);
//            return 0;
//        }
//
//        return status;
//    }

    private class CuriosityMapAdapter extends TypeAdapter<Map<Curiosity, Integer>> {
        @Override
        public void write(JsonWriter out, Map<Curiosity, Integer> map) throws IOException {
            out.beginObject();
            for (Curiosity curiosity: map.keySet()) {
                out.name(curiosity.query).value(map.get(curiosity));
            }
            out.endObject();
        }

        @Override
        public Map<Curiosity, Integer> read(JsonReader in) throws IOException {
            Map<Curiosity, Integer> map = new HashMap<>();

            in.beginObject();
            while (in.hasNext()) {
                Curiosity curiosity = new Curiosity(in.nextName());
                Integer status = in.nextInt();

                map.put(curiosity, status);
            }
            in.endObject();

            return map;
        }
    }
}