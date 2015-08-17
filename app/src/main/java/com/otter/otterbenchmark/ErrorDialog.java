package com.otter.otterbenchmark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorDialog extends DialogFragment {
    private static final String TAG = ErrorDialog.class.getSimpleName();

    public static final String FRAGMENT_TAG = "error_dialog";

    private static final String BUNDLE_MESSAGE = "message";

    private String mMessage;

    public static ErrorDialog newInstance(String message) {
        ErrorDialog fragment = new ErrorDialog();

        Bundle args = new Bundle();
        args.putString(BUNDLE_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    public ErrorDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = getArguments().getString(BUNDLE_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error)
                .setMessage(mMessage)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });

        return builder.create();
    }
}
