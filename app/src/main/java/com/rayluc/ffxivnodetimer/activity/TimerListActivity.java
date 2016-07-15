package com.rayluc.ffxivnodetimer.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.databinding.ActivitySetTimersBinding;
import com.rayluc.ffxivnodetimer.util.Util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chris on 7/10/16.
 */
public class TimerListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TIME = 1;
    static final int COL_NAME = 2;
    static final int COL_SLOT = 3;
    static final int COL_ZONE = 4;
    static final int COL_COORD = 5;
    static final int COL_ENABLED = 6;
    static final int COL_OFFSET = 7;

    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_SLOT,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES,
            ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED,
            ProviderContracts.ItemEntry.COLUMN_OFFSET
    };

    private final Handler mHandler = new Handler();
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
