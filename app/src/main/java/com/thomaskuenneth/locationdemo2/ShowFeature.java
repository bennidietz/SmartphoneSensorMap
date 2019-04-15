package com.thomaskuenneth.locationdemo2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

public class ShowFeature extends AppCompatActivity implements OnMapReadyCallback {

    DBHandler dbHandler;
    Context context = this;
    Map<String, String> feature = null;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;
    Button route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new DBHandler(this);
        setContentView(R.layout.activity_show_feature);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = (getResources().getDisplayMetrics().heightPixels)*35/100;
        mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);
        route = findViewById(R.id.route);
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + feature.get(DBHandler.OVERALL_LATITUDE) + "," +
                                feature.get(DBHandler.OVERALL_LONGITUDE)));
                startActivity(intent);
            }
        });
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
            } else {
                if (extras.containsKey(DBHandler.OVERALL_IDENTIFIER)) {
                    String identifer = extras.getString(DBHandler.OVERALL_IDENTIFIER);
                    ArrayList<Map<String, String>> records = dbHandler.executeSQLToDict(context, DBHandler.table1_keys, "select * from " + DBHandler.table1_keys[0] +
                            " where " + DBHandler.OVERALL_IDENTIFIER + " = " + identifer);
                    if (records.size() > 0) {
                        feature = records.get(0);
                    }
                }
            }
        }
        if (feature != null) {
            getSupportActionBar().setTitle(feature.get(DBHandler.TABLE1_C1));
            getSupportActionBar().setSubtitle(feature.get(DBHandler.OVERALL_CLASS));
        } else {
            Toast.makeText(context, "Der Datensatz konnte nicht gefunden werden", Toast.LENGTH_LONG);
            this.finish();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(Double.parseDouble(feature.get(DBHandler.OVERALL_LATITUDE)), Double.parseDouble(feature.get(DBHandler.OVERALL_LONGITUDE)));
        addMarkerToMap(latLng);

    }


    public void addMarkerToMap(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title(feature.get(DBHandler.TABLE1_C1));
        mMap.getUiSettings().setZoomControlsEnabled(true); // Kontrollelemente für Benutzer anzegien
        mMap.getUiSettings().setCompassEnabled(true); // zeige den Kompass an (wenn Karte gedreht)
        mMap.getUiSettings().setMapToolbarEnabled(true); // kontextabhängige Werkzeugleiste -> ImageButtons zu Google Maps (+ schnelle Route)
        //options.icon(BitmapDescriptorFactory.defaultMarker(
        //      BitmapDescriptorFactory.HUE_RED));
        options.icon(BitmapDescriptorFactory.defaultMarker());
        Marker marker = mMap.addMarker(options);
        CameraUpdate cu3 = CameraUpdateFactory.newLatLngZoom(options.getPosition(), 17);
        mMap.moveCamera(cu3);
    }
}
