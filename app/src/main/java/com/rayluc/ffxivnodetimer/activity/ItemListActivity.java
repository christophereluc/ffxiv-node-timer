package com.rayluc.ffxivnodetimer.activity;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rayluc.ffxivnodetimer.Constants;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.data.AsyncQueryHandlerWithCallback;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.databinding.ActivityItemListBinding;
import com.rayluc.ffxivnodetimer.databinding.CardNodeItemBinding;
import com.rayluc.ffxivnodetimer.model.NodeItem;
import com.rayluc.ffxivnodetimer.timer.NotificationService;
import com.rayluc.ffxivnodetimer.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ItemListActivity extends AppCompatActivity implements AsyncQueryHandlerWithCallback.QueryCallback, AlarmDialogFragment.DialogListener, LoaderManager.LoaderCallbacks<Cursor> {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TIME = 1;
    static final int COL_NAME = 2;
    static final int COL_SLOT = 3;
    static final int COL_ZONE = 4;
    static final int COL_COORD = 5;
    static final int COL_ENABLED = 6;

    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_SLOT,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES,
            ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED
    };
    //Firebase Analytics object
    private FirebaseAnalytics mFirebaseAnalytics;
    private final Handler mHandler = new Handler();
    //Data binding binder
    private ActivityItemListBinding mBinding;
    private RecyclerViewAdapter mAdapter;
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
        // Obtain the FirebaseAnalytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_item_list);
        configureUi();
        setSupportActionBar(mBinding.toolbar);

        getLoaderManager().initLoader(0, null, this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }

    /**
     * Exclude configure timer from this.  We want that only running during onstart through onstop
     */
    private void configureUi() {
        configureAd();
        configureRecyclerView();
    }

    //Configures Ads
    private void configureAd() {
        AdView adView = (AdView) findViewById(R.id.adView);
        if (adView != null) {
            MobileAds.initialize(getApplicationContext(), "ca-app-pub-8040512079233964~8255527732");

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                    .addTestDevice("SomeString")  // An example device ID
                    .build();
            adView.loadAd(adRequest);
        }
    }

    //Configures the recyclerview
    private void configureRecyclerView() {
        mAdapter = new RecyclerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_items), LinearLayoutManager.VERTICAL, false);
        gridLayoutManager.setAutoMeasureEnabled(true);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);
        mBinding.recyclerview.setAdapter(mAdapter);
        mBinding.recyclerview.setNestedScrollingEnabled(false);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.RESULT_DELETED) {
            getLoaderManager().restartLoader(0, null, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onFabClicked(View v) {
        startActivityForResult(new Intent(this, TimerListActivity.class), 0);
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

    }

    @Override
    public void onInsertClicked(int id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(this, NotificationService.class);
        newIntent.putExtra(Constants.NODE_ID, id);
        PendingIntent pendingIntent = PendingIntent.getService(this, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long alarm = Util.getNextRealTimeInMillis(mAdapter.getItemById(id).time);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);

        Uri uri = ProviderContracts.ItemEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED, 1);
        new AsyncQueryHandlerWithCallback(getContentResolver(), this).startUpdate(id, null, uri, values, ProviderContracts.ItemEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public void onRemoveTimerClicked(int id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent newIntent = new Intent(this, NotificationService.class);
        newIntent.putExtra(Constants.NODE_ID, id);

        PendingIntent pendingIntent = PendingIntent.getService(this, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        Uri uri = ProviderContracts.ItemEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED, 0);
        new AsyncQueryHandlerWithCallback(getContentResolver(), this).startUpdate(0, null, uri, values, ProviderContracts.ItemEntry._ID + " = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                ProviderContracts.ItemEntry.CONTENT_URI,
                ITEM_COLUMS,
                null,
                null,
                ProviderContracts.ItemEntry.COLUMN_TIME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mAdapter.setData(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.setData(null);
    }


    protected class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<NodeItem> mData;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_node_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NodeItem item = mData.get(position);
            holder.cardItemBinding.setItem(item);
        }

        public NodeItem getItemById(int id) {
            if (mData != null) {
                for (NodeItem item : mData) {
                    if (item.id == id) {
                        return item;
                    }
                }
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return mData != null ? mData.size() : 0;
        }

        public void setData(Cursor cursor) {
            mData = new ArrayList<>();
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    mData.add(getNodeFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            notifyDataSetChanged();
        }

        private NodeItem getNodeFromCursor(Cursor data) {
            return new NodeItem(data.getInt(COL_ID),
                    data.getString(COL_TIME),
                    data.getString(COL_NAME),
                    data.getInt(COL_SLOT),
                    data.getString(COL_ZONE),
                    data.getString(COL_COORD),
                    data.getInt(COL_ENABLED) == 1);
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            protected CardNodeItemBinding cardItemBinding;

            public ViewHolder(View itemView) {
                super(itemView);
                cardItemBinding = DataBindingUtil.bind(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new AlarmDialogFragment();
                NodeItem currentNode = mData.get(getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.NODE, currentNode);
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "alarm");
                // Log which item was selected in Firebase Analytics
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.ITEM_ID, currentNode.name);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
                mFirebaseAnalytics.logEvent("test_event", params);
            }
        }
    }
}
