package com.rayluc.ffxivnodetimer.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by chris on 7/10/16.
 */
public class ProviderContracts {

    public static final int MINER = 0;
    public static final int BOTANIST = 1;
    public static final String CONTENT_AUTHORITY = "com.rayluc.ffxivnodetimer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEM = "item";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MINER, BOTANIST})

    public @interface Disciples {

    }

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

        // Table name
        public static final String TABLE_NAME = "item";


        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SLOT = "slot";
        public static final String COLUMN_ZONE = "zone";
        public static final String COLUMN_COORDINATES = "coordinates";
        public static final String COLUMN_DISCIPLE = "disciple";
        public static final String COLUMN_OFFSET = "offset";
        public static final String COLUMN_TIMER_ENABLED = "timeron";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}