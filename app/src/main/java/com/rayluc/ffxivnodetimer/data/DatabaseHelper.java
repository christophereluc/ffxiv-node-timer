package com.rayluc.ffxivnodetimer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rayluc.ffxivnodetimer.model.NodeItem;

/**
 * Created by chris on 7/10/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "node.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ITEM_TABLE = "CREATE TABLE " + ProviderContracts.ItemEntry.TABLE_NAME + " (" +
                ProviderContracts.ItemEntry._ID + " INTEGER PRIMARY KEY," +
                ProviderContracts.ItemEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                ProviderContracts.ItemEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProviderContracts.ItemEntry.COLUMN_SLOT + " INTEGER NOT NULL, " +
                ProviderContracts.ItemEntry.COLUMN_ZONE + " TEXT NOT NULL, " +
                ProviderContracts.ItemEntry.COLUMN_COORDINATES + " TEXT NOT NULL, " +
                ProviderContracts.ItemEntry.COLUMN_DISCIPLE + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_TIMER_TABLE = "CREATE TABLE " + ProviderContracts.TimerEntry.TABLE_NAME + " (" +

                ProviderContracts.TimerEntry._ID + " INTEGER PRIMARY KEY," +

                // the ID of the location entry associated with this weather data
                ProviderContracts.TimerEntry.COLUMN_ITEM_KEY + " INTEGER NOT NULL, " +
                ProviderContracts.TimerEntry.COLUMN_ALARM_TIME + " INTEGER NOT NULL, " +
                ProviderContracts.TimerEntry.COLUMN_ALARM_OFFSET + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + ProviderContracts.TimerEntry.COLUMN_ITEM_KEY + ") REFERENCES " +
                ProviderContracts.ItemEntry.TABLE_NAME + " (" + ProviderContracts.ItemEntry._ID + "), " +

                //Only allow one timer per entry
                " UNIQUE (" + ProviderContracts.TimerEntry.COLUMN_ITEM_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ITEM_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TIMER_TABLE);
        insertRows(sqLiteDatabase);
    }

    private void insertRows(SQLiteDatabase database) {
        if (database == null) {
            database = getWritableDatabase();
        }

        database.beginTransaction();
        for (NodeItem miner : DefaultQueries.MINER_ITEMS) {
            ContentValues value = new ContentValues();
            value.put(ProviderContracts.ItemEntry._ID, miner.id);
            value.put(ProviderContracts.ItemEntry.COLUMN_NAME, miner.name);
            value.put(ProviderContracts.ItemEntry.COLUMN_ZONE, miner.zone);
            value.put(ProviderContracts.ItemEntry.COLUMN_TIME, miner.time);
            value.put(ProviderContracts.ItemEntry.COLUMN_SLOT, miner.slot);
            value.put(ProviderContracts.ItemEntry.COLUMN_COORDINATES, miner.coord);
            value.put(ProviderContracts.ItemEntry.COLUMN_DISCIPLE, miner.disciple);
            database.insert(ProviderContracts.ItemEntry.TABLE_NAME, null, value);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProviderContracts.TimerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProviderContracts.ItemEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
