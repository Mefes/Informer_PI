package com.example.vladimir.informer_pi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/**
 * Created by Vladimir Kadochnikov on 06.11.15.
 */
public class SettingsActivity extends Activity {
    String[] ArrayCourse = {"1 курс", "2 курс", "3 курс", "4 курс", "5 курс", "6 курс"};
    String[] ArrayGroup = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final Intent intent = getIntent();
        Integer Course = intent.getIntExtra("course", -1);
        Integer Group = intent.getIntExtra("group", -1);
        final Spinner SpinnerCourse = (Spinner) findViewById(R.id.spinner);
        final Spinner SpinnerGroup = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<String> AdapterCourse = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ArrayCourse);
        AdapterCourse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerCourse.setAdapter(AdapterCourse);
        SpinnerCourse.setSelection(Course);

        ArrayAdapter<String> AdapterGroup = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ArrayGroup);
        AdapterGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerGroup.setAdapter(AdapterGroup);
        SpinnerGroup.setSelection(Group);
        Button ButtonOk = (Button) findViewById(R.id.buttonSave);
        ButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("course", SpinnerCourse.getSelectedItemPosition());
                intent.putExtra("group", SpinnerGroup.getSelectedItemPosition());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        Button ButtonCancel = (Button) findViewById(R.id.buttonCancel);
        ButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

}
