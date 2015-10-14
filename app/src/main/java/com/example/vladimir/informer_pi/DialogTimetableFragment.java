package com.example.vladimir.informer_pi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Vladimir Kadochnikov on 10.10.15.
 */
public class DialogTimetableFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] courseArray = {"1 курс", "2 курс", "3 курс","4 курс"};
        final boolean[] checkedItemsArray = {false, false, false,false};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Выберите ваш курс")
                .setMultiChoiceItems(courseArray, checkedItemsArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {


                                checkedItemsArray[which] = isChecked;
                            }
                        })
                .setPositiveButton("Готово",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                for (int i = 0; i < courseArray.length; i++) {
                                    if (checkedItemsArray[i])
                                    ((MainActivity) getActivity()).course = i;
                                    ((MainActivity) getActivity()).setPreference();
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

        return builder.create();
    }

}
