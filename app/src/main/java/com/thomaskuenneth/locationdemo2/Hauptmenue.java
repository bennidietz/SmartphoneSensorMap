package com.thomaskuenneth.locationdemo2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Hauptmenue extends AppCompatActivity {

    ImageButton zuKarte;
    Button addFeature;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hauptmenue);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        zuKarte = findViewById(R.id.zuKarte);
        zuKarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent zuKarte = new Intent(context, LocationDemo2Activity.class);
                startActivity(zuKarte);
            }
        });
        addFeature = findViewById(R.id.addFeature);
        addFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent zuKarte = new Intent(context, AddMarker.class);
                startActivity(zuKarte);
            }
        });
    }

}
