/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth;

/**
 * Created by pawan on 7/8/16.
 */
public class Constants {
    // Curiosity Status
    public static Integer STATUS_INITIAL                = 0x000;
    public static Integer STATUS_HAS_RESULTS            = 0x001;
    public static Integer STATUS_HAS_UNREAD_RESULTS     = 0x002;
    public static Integer STATUS_QUERYING_FINISHED      = 0x004;

    // Curiosity List Status
    public static Integer STATUS_NO_CURIOSITIES         = 0x010;

    // Network Status
    public static Integer STATUS_UNMETERED_NETWORK      = 0x100;
    public static Integer STATUS_METERED_NETWORK        = 0x200;

    // ViewHolder Types
    public static final int TYPE_STATUS     =   0;
    public static final int TYPE_CURIOSITY  =   1;
}
