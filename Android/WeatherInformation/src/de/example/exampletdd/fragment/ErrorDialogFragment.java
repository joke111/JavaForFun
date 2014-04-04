package de.example.exampletdd.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import de.example.exampletdd.R;

public class ErrorDialogFragment extends DialogFragment {

    public static ErrorDialogFragment newInstance(final int title) {
        final ErrorDialogFragment frag = new ErrorDialogFragment();
        final Bundle args = new Bundle();

        args.putInt("title", title);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final int title = this.getArguments().getInt("title");

        return new AlertDialog.Builder(this.getActivity())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setPositiveButton(R.string.button_ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int whichButton) {

            }
        }).create();
    }
}