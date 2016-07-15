package com.rayluc.ffxivnodetimer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.rayluc.ffxivnodetimer.Constants;
import com.rayluc.ffxivnodetimer.R;
import com.rayluc.ffxivnodetimer.data.AsyncQueryHandlerWithCallback;
import com.rayluc.ffxivnodetimer.model.NodeItem;

/**
 * Created by Raymond on 7/13/2016.
 */
public class AlarmDialogFragment extends android.support.v4.app.DialogFragment {

    private AsyncQueryHandlerWithCallback mQuery;
    private String nodeIdStr;
    private DialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogListener) context;
        } catch (ClassCastException e) {
            //Listener not implemented
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Get arguments if there are any
        Bundle bundle = getArguments();
        final NodeItem node = bundle.getParcelable(Constants.NODE);

        if (node != null) {
            //Inflate and set the layout for the dialog
            //Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_set_alarm, null))
                    .setMessage(getString(R.string.alarm_message, node.name))
                    .setPositiveButton(R.string.set_alarm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Update NodeItem
                            EditText mEdit = (EditText) getDialog().findViewById(R.id.alarmSetter);
                            int minutes = Integer.parseInt(mEdit.getText().toString());
                            if (mListener != null) {
                                mListener.onInsertClicked(node.id, minutes);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AlarmDialogFragment.this.getDialog().cancel();
                        }
                    });
            if (node.timerEnabled.get()) {
                builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mListener != null) {
                            mListener.onRemoveTimerClicked(node.id);
                        }
                    }
                });
            }

        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface DialogListener {
        void onInsertClicked(int id, int minutesOffset);

        void onRemoveTimerClicked(int id);
    }


}

