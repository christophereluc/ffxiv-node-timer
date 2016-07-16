package com.rayluc.ffxivnodetimer.activity;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.rayluc.ffxivnodetimer.Constants;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.application.CoreApplication;
import com.rayluc.ffxivnodetimer.data.AsyncQueryHandlerWithCallback;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.databinding.ActivitySetTimersBinding;
import com.rayluc.ffxivnodetimer.databinding.CardTimerItemBinding;
import com.rayluc.ffxivnodetimer.model.NodeItem;
import com.rayluc.ffxivnodetimer.timer.NotificationService;
import com.rayluc.ffxivnodetimer.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class TimerListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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
    private final Handler mHandler = new Handler();
    private TimerViewAdapter mAdapter;
    //Data binding binder
    private ActivitySetTimersBinding mBinding;
    //Timer task to call function to update textview
    private Timer mTimer;
    //Date formatter for when date is displayed
    private SimpleDateFormat simpleDateFormat;
    //Runnable that updates the textview on the UI thread
    private final Runnable updateTextRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder stringBuilder = new StringBuilder(getString(R.string.current_time)).append(" ").append(simpleDateFormat.format(Util.getEorzeanTime()));
            mBinding.timeText.setText(stringBuilder);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_set_timers);
        configureUi();
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Ensure we're not leaking anything
        ((CoreApplication) getApplication()).getRefWatcher().watch(mTimer);

    }

    @Override
    protected void onStart() {
        super.onStart();
        configureTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * Exclude configure timer from this.  We want that only running during onstart through onstop
     */
    private void configureUi() {
        configureAd();
        configureRecyclerView();
    }

    //Configures the recyclerview
    private void configureRecyclerView() {
        mAdapter = new TimerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_items), LinearLayoutManager.VERTICAL, false);
        gridLayoutManager.setAutoMeasureEnabled(true);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);
        mBinding.recyclerview.setAdapter(mAdapter);
        mBinding.recyclerview.setNestedScrollingEnabled(false);
    }

    //Configures Ads
    private void configureAd() {
        AdView adView = (AdView) findViewById(R.id.adView);
        if (adView != null) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice("SomeString")  // An example device ID
                    .build();
            adView.loadAd(adRequest);
        }
    }

    //Configures Eorzean Timer text view
    //Should only run onstart through onstop (we don't need to update the ui when its stopped)
    private void configureTimer() {
        simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);
    }

    //Called by the timer task
    private void updateTime() {
        mHandler.post(updateTextRunnable);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                ProviderContracts.ItemEntry.CONTENT_URI,
                ITEM_COLUMS,
                ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED + " = ?",
                new String[]{"1"},
                ProviderContracts.ItemEntry.COLUMN_TIME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            mAdapter.setData(cursor);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.no_timers))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            finish();
                        }
                    }).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    protected class TimerViewAdapter extends RecyclerView.Adapter<TimerViewAdapter.ViewHolder> {

        private ArrayList<NodeItem> mData;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_timer_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NodeItem item = mData.get(position);
            holder.cardItemBinding.setItem(item);
        }

        @Override
        public int getItemCount() {
            return mData != null ? mData.size() : 0;
        }

        public void setData(Cursor cursor) {
            mData = new ArrayList<>();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    mData.add(getNodeFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            notifyDataSetChanged();
        }

        private NodeItem getNodeFromCursor(Cursor data) {
            NodeItem nodeItem = new NodeItem();
            nodeItem.id = data.getInt(COL_ID);
            nodeItem.time = data.getString(COL_TIME);
            nodeItem.name = data.getString(COL_NAME);
            nodeItem.zone = data.getString(COL_ZONE);
            nodeItem.coord = data.getString(COL_COORD);
            return nodeItem;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AsyncQueryHandlerWithCallback.QueryCallback {

            protected CardTimerItemBinding cardItemBinding;

            public ViewHolder(View itemView) {
                super(itemView);
                cardItemBinding = DataBindingUtil.bind(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                final int position = getAdapterPosition();
                final NodeItem item = mData.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(TimerListActivity.this)
                        .setTitle(getString(R.string.caution))
                        .setMessage(getString(R.string.delete_item, item.name))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri uri = ProviderContracts.ItemEntry.CONTENT_URI;
                                ContentValues values = new ContentValues();
                                values.put(ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED, 0);
                                new AsyncQueryHandlerWithCallback(getContentResolver(), ViewHolder.this).startUpdate(0, null, uri, values, ProviderContracts.ItemEntry._ID + " = ?", new String[]{String.valueOf(item.id)});
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            }

            @Override
            public void onInsertComplete(boolean successful) {

            }

            @Override
            public void onDeleteComplete(boolean successful) {

            }

            @Override
            public void onQueryComplete(Cursor cursor) {

            }

            @Override
            public void onUpdateComplete(int id) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent newIntent = new Intent(TimerListActivity.this, NotificationService.class);
                newIntent.putExtra(Constants.NODE_ID, id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(TimerListActivity.this, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}
