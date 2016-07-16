package com.rayluc.ffxivnodetimer.timer;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

import com.rayluc.ffxivnodetimer.Constants;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.activity.ItemListActivity;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.model.NodeItem;
import com.rayluc.ffxivnodetimer.util.Util;

public class NotificationService extends IntentService {

    //Database items
    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_ZONE = 2;
    static final int COL_COORD = 3;
    static final int COL_TIME = 4;

    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES,
            ProviderContracts.ItemEntry.COLUMN_TIME
    };


    public NotificationService() {
        super(NotificationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            int id = intent.getIntExtra(Constants.NODE_ID, -1);

            if (id > -1) {
                Cursor cursor = getContentResolver().query(ProviderContracts.ItemEntry.CONTENT_URI, ITEM_COLUMS, ProviderContracts.ItemEntry._ID + " = ?", new String[]{String.valueOf(id)}, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        NodeItem nodeItem = createNodeFromCursor(cursor);
                        displayNotification(nodeItem);
                        restartTimer(nodeItem);
                    }
                    cursor.close();
                }
            }
        }
    }


    private void displayNotification(NodeItem item) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_alarm_white)
                        .setContentTitle(getString(R.string.title, item.name))
                        .setContentText(getString(R.string.message, item.getFormattedZoneCoord()));
        Intent resultIntent = new Intent(this, ItemListActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(item.id, mBuilder.build());
    }

    private NodeItem createNodeFromCursor(Cursor c) {
        NodeItem nodeItem = new NodeItem();
        nodeItem.coord = c.getString(COL_COORD);
        nodeItem.id = c.getInt(COL_ID);
        nodeItem.name = c.getString(COL_NAME);
        nodeItem.zone = c.getString(COL_ZONE);
        nodeItem.time = c.getString(COL_TIME);

        return nodeItem;
    }

    private void restartTimer(NodeItem item) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(this, NotificationService.class);
        newIntent.putExtra(Constants.NODE_ID, item.id);
        PendingIntent pendingIntent = PendingIntent.getService(this, item.id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long alarm = Util.getNextRealTimeInMillis(item.time);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
    }
}
