/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import timber.log.Timber;


/**
 * Created by pawan on 4/6/16.
 */
public class FileUtil {
    public static byte[] readFromStream(final File file) throws IOException {
        if (!file.exists()) {
            Timber.e("File %s does not exist", file);
        }

        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        int size = inputStream.available();
        byte[] buffer = new byte[size];

        inputStream.read(buffer);
        inputStream.close();

        return buffer;
    }

    public static String byteArrayToString(final byte[] byteArray) throws UnsupportedEncodingException {
        String string = new String(byteArray, "UTF-8");

        return string;
    }

    public static Bitmap byteArrayToBitmap(final byte[] byteArray) throws UnsupportedEncodingException {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        return bitmap;
    }

    public static void writeToFile(final File file, final String text) throws IOException {
        if (file.exists()) {
            file.delete();
        }

        FileWriter writer = new FileWriter(file);

        writer.write(text);

        writer.close();
    }

    public static void writeToFile(final File file, final Bitmap image) throws IOException {
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream outputStream = new FileOutputStream(file);
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        Timber.d("Saved to outputStream: %s of file %s", outputStream, file.exists());
    }

    public static boolean deleteFileRecursively(File rootFile) {
        if (rootFile.isDirectory()) {
            for (File file: rootFile.listFiles()) {
                deleteFileRecursively(file);
            }
        }

        return rootFile.delete();
    }
}
