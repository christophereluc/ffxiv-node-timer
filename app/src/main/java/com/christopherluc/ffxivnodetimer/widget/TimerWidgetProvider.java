package com.christopherluc.ffxivnodetimer.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.christopherluc.ffxivnodetimer.R;
import com.christopherluc.ffxivnodetimer.activity.ItemListActivity;

public class TimerWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // construct the RemoteViews object
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_timer_list);
            // set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                setRemoteAdapter(context, remoteViews);
            else
                setRemoteAdapterV11(context, remoteViews);

            Intent startActivityIntent = new Intent(context, ItemListActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, startActivityPendingIntent);

            // instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Intent intent = new Intent(context, TimerWidgetRemoteViewsServices.class);
        views.setRemoteAdapter(R.id.widget_list, intent);
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        Intent intent = new Intent(context, TimerWidgetRemoteViewsServices.class);
        views.setRemoteAdapter(0, R.id.widget_list, intent);
    }
}
