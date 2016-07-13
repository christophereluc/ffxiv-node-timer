package com.rayluc.ffxivnodetimer.activity;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.data.AsyncQueryHandlerWithCallback;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.databinding.ActivityItemListBinding;
import com.rayluc.ffxivnodetimer.databinding.CardItemBinding;
import com.rayluc.ffxivnodetimer.model.NodeItem;
import com.rayluc.ffxivnodetimer.util.Util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ItemListActivity extends AppCompatActivity implements AsyncQueryHandlerWithCallback.QueryCallback {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TIME = 1;
    static final int COL_NAME = 2;
    static final int COL_SLOT = 3;
    static final int COL_ZONE = 4;
    static final int COL_COORD = 5;
    static final int COL_DISC = 6;
    // Identifies a particular Loader being used in this component
    private static final int QUERY_ID = 0;
    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_SLOT,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES,
            ProviderContracts.ItemEntry.COLUMN_DISCIPLE
    };
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

    private AsyncQueryHandlerWithCallback mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_item_list);
        configureUi();
        setSupportActionBar(mBinding.toolbar);
        mQuery = new AsyncQueryHandlerWithCallback(getContentResolver(), this);
        mQuery.startQuery(QUERY_ID, null,
                ProviderContracts.ItemEntry.CONTENT_URI,
                ITEM_COLUMS,
                null,
                null,
                ProviderContracts.ItemEntry.COLUMN_TIME + " ASC");
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
        if (mQuery != null) {
            mQuery.cancelOperation(QUERY_ID);
        }
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
        simpleDateFormat = new SimpleDateFormat("HH:mm");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFabClicked(View v) {
        Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onInsertComplete(boolean successful) {

    }

    @Override
    public void onDeleteComplete(boolean successful) {

    }

    @Override
    public void onQueryComplete(Cursor cursor) {
        mAdapter.setData(cursor);
    }

    protected class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private Cursor mData;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mData.moveToPosition(position)) {
                NodeItem item = getNodeFromCursor(mData);
                holder.cardItemBinding.setItem(item);
            }
        }

        @Override
        public int getItemCount() {
            return mData != null ? mData.getCount() : 0;
        }

        public void setData(Cursor cursor) {
            if (mData != null) {
                mData.close();
            }
            mData = cursor;
        }

        private NodeItem getNodeFromCursor(Cursor data) {
            return new NodeItem(data.getInt(COL_ID),
                    data.getString(COL_TIME),
                    data.getString(COL_NAME),
                    data.getInt(COL_SLOT),
                    data.getString(COL_ZONE),
                    data.getString(COL_COORD),
                    data.getInt(COL_DISC));
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {

            protected CardItemBinding cardItemBinding;

            public ViewHolder(View itemView) {
                super(itemView);
                cardItemBinding = DataBindingUtil.bind(itemView);
            }
        }
    }
}
