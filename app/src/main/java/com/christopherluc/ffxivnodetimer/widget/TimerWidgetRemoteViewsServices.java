package com.christopherluc.ffxivnodetimer.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.christopherluc.ffxivnodetimer.R;
import com.christopherluc.ffxivnodetimer.data.ProviderContracts;
import com.christopherluc.ffxivnodetimer.util.Util;

public class TimerWidgetRemoteViewsServices extends RemoteViewsService {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TIME = 1;
    static final int COL_NAME = 2;
    static final int COL_ZONE = 3;
    static final int COL_COORD = 4;

    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES
    };


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // return remote view factory
        return new RemoteViewsFactory() {
            Cursor mCursor = null;

            @Override
            public void onCreate() {
                // nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null)
                    mCursor.close();
                long identityToken = Binder.clearCallingIdentity();
                mCursor = getContentResolver().query(ProviderContracts.ItemEntry.CONTENT_URI,
                        ITEM_COLUMS,
                        ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_timer_item);
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    remoteViews.setTextViewText(R.id.name, mCursor.getString(COL_NAME));
                    remoteViews.setTextViewText(R.id.zone, mCursor.getString(COL_ZONE) + ": " + mCursor.getString(COL_COORD));
                    remoteViews.setTextViewText(R.id.time, Util.convert24HourToAmPm(mCursor.getString(COL_TIME)));
                }
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}