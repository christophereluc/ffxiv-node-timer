package com.christopherluc.ffxivnodetimer.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.christopherluc.ffxivnodetimer.Constants;
import com.christopherluc.ffxivnodetimer.data.ProviderContracts;
import com.christopherluc.ffxivnodetimer.util.Util;

public class BootReceiver extends BroadcastReceiver {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TIME = 1;

    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
    };

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            Cursor cursor = context.getContentResolver().query(ProviderContracts.ItemEntry.CONTENT_URI, ITEM_COLUMS, ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED + " = ?", new String[]{"1"}, null);
            if (cursor != null && cursor.getCount() > 0) {
                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                cursor.moveToFirst();
                do {

                    long alarm = Util.getNextRealTimeInMillis(cursor.getString(COL_TIME));
                    Intent newIntent = new Intent(context, NotificationService.class);
                    int id = cursor.getInt(COL_ID);
                    newIntent.putExtra(Constants.NODE_ID, id);

                    PendingIntent pendingIntent = PendingIntent.getService(context, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.setExact(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }

    }
}
