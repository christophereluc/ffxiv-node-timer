package com.rayluc.ffxivnodetimer.activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.data.AsyncQueryHandlerWithCallback;
import com.rayluc.ffxivnodetimer.data.DataProvider;
import com.rayluc.ffxivnodetimer.data.DatabaseHelper;
import com.rayluc.ffxivnodetimer.data.ProviderContracts;

/**
 * Created by Raymond on 7/13/2016.
 */
public class AlarmDialogFragment extends android.support.v4.app.DialogFragment implements AsyncQueryHandlerWithCallback.QueryCallback {

    private AsyncQueryHandlerWithCallback mQuery;
    String nodeIdStr;
    private static final String TAG = "AlarmDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Get arguments if there are any
        Bundle bundle = this.getArguments();
        String nodeName = null;
        Integer nodeId;
        if (bundle != null) {
            nodeName = bundle.getString("nodename");
            nodeId = bundle.getInt("nodeId");
            nodeIdStr = nodeId.toString();
        }

        mQuery = new AsyncQueryHandlerWithCallback(getActivity().getContentResolver(), this);

        //Inflate and set the layout for the dialog
        //Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_set_alarm, null))
                .setMessage("Enable timer for " + nodeName + "?")
                .setPositiveButton(R.string.set_alarm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Update NodeItem
                        EditText mEdit = (EditText) getDialog().findViewById(R.id.alarmSetter);
                        int minutes = Integer.parseInt(mEdit.getText().toString());
                        Uri uri = ProviderContracts.ItemEntry.CONTENT_URI;
                        ContentValues values = new ContentValues();
                        values.put(ProviderContracts.ItemEntry.COLUMN_OFFSET, minutes);
                        values.put(ProviderContracts.ItemEntry.COLUMN_TIMER_ENABLED, 1);
                        mQuery.startUpdate(0, null, uri, values, ProviderContracts.ItemEntry._ID + " = ?", new String[] {nodeIdStr});

                        //Query and print DB values in LogCat to show successful update


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlarmDialogFragment.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
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


}

