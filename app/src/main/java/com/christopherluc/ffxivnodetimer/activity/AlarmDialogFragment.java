package com.christopherluc.ffxivnodetimer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.christopherluc.ffxivnodetimer.Constants;
import com.christopherluc.ffxivnodetimer.R;
import com.christopherluc.ffxivnodetimer.model.NodeItem;

public class AlarmDialogFragment extends android.support.v4.app.DialogFragment {

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

        //Get arguments if there are any
        Bundle bundle = getArguments();
        final NodeItem node = bundle.getParcelable(Constants.NODE);

        if (node != null) {
            if (node.timerEnabled.get()) {
                builder.setMessage(getString(R.string.delete_item, node.name))
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mListener != null) {
                                    mListener.onRemoveTimerClicked(node.id);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AlarmDialogFragment.this.getDialog().cancel();
                            }
                        });
            } else {
                builder.setMessage(getString(R.string.alarm_message, node.name))
                        .setPositiveButton(R.string.set_alarm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mListener.onInsertClicked(node.id);
                                dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AlarmDialogFragment.this.getDialog().cancel();
                            }
                        });
            }

        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface DialogListener {
        void onInsertClicked(int id);

        void onRemoveTimerClicked(int id);
    }


}

