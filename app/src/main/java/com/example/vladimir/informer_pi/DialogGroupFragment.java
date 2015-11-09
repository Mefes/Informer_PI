package com.example.vladimir.informer_pi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Vladimir Kadochnikov on 31.10.15.
 */

public class DialogGroupFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] groupArray = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        final boolean[] checkedGroupItemsArray = {false, false, false, false, false, false, false, false, false, false};
        AlertDialog.Builder builderGroup = new AlertDialog.Builder(getActivity());
        builderGroup.setTitle("Выберите вашу группу")
                .setMultiChoiceItems(groupArray, checkedGroupItemsArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {


                                checkedGroupItemsArray[which] = isChecked;
                            }
                        })
                .setPositiveButton("Готово",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                for (int i = 0; i < groupArray.length; i++) {
                                    if (checkedGroupItemsArray[i]) {
                                        ((MainActivity) getActivity()).Group = i;
                                        ((MainActivity) getActivity()).setPreference();
                                    }
                                }
                            }
                        })

                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        return builderGroup.create();
    }
}