package com.thomaskuenneth.locationdemo2;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;

public class AddMarker extends AppCompatActivity {


    EditText klasse, name;
    Button ok;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        klasse = findViewById(R.id.klasse);
        name = findViewById(R.id.name);
        ok = findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHandler dbHandler = new DBHandler(context);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(DBHandler.OVERALL_CLASS, klasse.getText().toString());
                hashMap.put(DBHandler.TABLE1_C1, name.getText().toString());
                dbHandler.addRecord(hashMap, DBHandler.table1_keys);
            }
        });

    }

}
