package com.rayluc.ffxivnodetimer.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.rayluc.ffxivnodetimer.data.ProviderContracts;
import com.rayluc.ffxivnodetimer.databinding.ActivityItemListBinding;
import com.rayluc.ffxivnodetimer.databinding.CardItemBinding;
import com.rayluc.ffxivnodetimer.model.NodeItem;

public class ItemListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_ID = 0;
    static final int COL_TIME = 1;
    static final int COL_NAME = 2;
    static final int COL_SLOT = 3;
    static final int COL_ZONE = 4;
    static final int COL_COORD = 5;
    static final int COL_DISC = 6;
    // Identifies a particular Loader being used in this component
    private static final int LOADER = 0;
    private static final String[] ITEM_COLUMS = {
            ProviderContracts.ItemEntry.TABLE_NAME + "." + ProviderContracts.ItemEntry._ID,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_NAME,
            ProviderContracts.ItemEntry.COLUMN_TIME,
            ProviderContracts.ItemEntry.COLUMN_SLOT,
            ProviderContracts.ItemEntry.COLUMN_ZONE,
            ProviderContracts.ItemEntry.COLUMN_COORDINATES,
            ProviderContracts.ItemEntry.COLUMN_DISCIPLE
    };
    private ActivityItemListBinding mBinding;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_item_list);
        mAdapter = new RecyclerViewAdapter();
        mBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerview.setAdapter(mAdapter);
        setSupportActionBar(mBinding.toolbar);
        getLoaderManager().initLoader(LOADER, null, this);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8040512079233964~8255527732");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("SomeString")  // An example device ID
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER:
                return new CursorLoader(this,
                        ProviderContracts.ItemEntry.CONTENT_URI,
                        ITEM_COLUMS,
                        null,
                        null,
                        ProviderContracts.ItemEntry.COLUMN_TIME + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setData(null);
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
