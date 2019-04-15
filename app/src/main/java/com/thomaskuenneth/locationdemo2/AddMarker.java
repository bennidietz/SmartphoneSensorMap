package com.thomaskuenneth.locationdemo2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.HashMap;

public class AddMarker extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {


    private static final int CAMERA_REQUEST_CODE = 234;
    EditText name;
    Spinner klasse;
    Button ok;
    Context context = this;
    private GoogleMap mMap;
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;
    Marker marker;
    SupportMapFragment mapFragment;
    Camera_Helper camera_helper;
    ArrayList<String> bilderaufgenommen;
    TextView anzahl_bilder;
    ImageButton camera;

    public static String[] kategorien = new String[]{"Mode", "Dekoration/ Einrichtung", "Kunst", "Kulinarisches"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        camera_helper = new Camera_Helper(this);
        bilderaufgenommen = new ArrayList<String>();
        anzahl_bilder = findViewById(R.id.anzahl_bilder);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = (getResources().getDisplayMetrics().heightPixels)*6/10;
        mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);
        klasse = findViewById(R.id.klasse);
        camera = (ImageButton) findViewById(R.id.camera);
        camera.setOnClickListener(this);
        ArrayAdapter<String> adapter_kategorien = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kategorien);
        klasse.setAdapter(adapter_kategorien);
        klasse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(23);

            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        name = findViewById(R.id.name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (name.getText().length() == 0) {
                    ok.setEnabled(false); ok.setTextColor(ContextCompat.getColor(context, R.color.disabledblack));
                } else {
                    ok.setEnabled(true); ok.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
            }
        });
        ok = findViewById(R.id.ok);
        ok.setEnabled(false);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHandler dbHandler = new DBHandler(context);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(DBHandler.OVERALL_CLASS, klasse.getSelectedItem().toString());
                hashMap.put(DBHandler.TABLE1_C1, name.getText().toString());
                hashMap.put(DBHandler.OVERALL_LATITUDE, marker.getPosition().latitude + "");
                hashMap.put(DBHandler.OVERALL_LONGITUDE, marker.getPosition().longitude + "");
                if (bilderaufgenommen.size() > 0) {
                    hashMap.put(DBHandler.OVERALL_PICTURE, Camera_Helper.getPictureIncludedString(bilderaufgenommen));
                }
                dbHandler.addRecord(hashMap, DBHandler.table1_keys);
                ((Activity)context).finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (camera_helper.onActivityResult(requestCode, resultCode, data) != null) {
            String bild = camera_helper.onActivityResult(requestCode, resultCode, data);
            bilderaufgenommen.add(bild);
            anzahl_bilder.setText(bilderaufgenommen.size() + "");
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerChanged(getMarkerOptionsFromLatLng(latLng));
            }
        });

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        } else {
            markerDemo();
        }

    }

    private void markerDemo() throws SecurityException {
        LocationManager m = getSystemService(LocationManager.class);
        if (m == null) {
            return;
        }
        Location loc =
                m.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc != null) {
            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate cu1 = CameraUpdateFactory.newLatLngZoom(pos, 16);
            mMap.moveCamera(cu1);
            markerChanged(getMarkerOptionsFromLatLng(pos));

        }
    }

    public void markerChanged(MarkerOptions newMarkerOptions) {
        if (marker != null) {
            marker.remove();
        }
        marker = mMap.addMarker(newMarkerOptions);
        mMap.getUiSettings().setZoomControlsEnabled(true); // Kontrollelemente für Benutzer anzegien
        mMap.getUiSettings().setCompassEnabled(true); // zeige den Kompass an (wenn Karte gedreht)
        mMap.getUiSettings().setMapToolbarEnabled(true); // kontextabhängige Werkzeugleiste -> ImageButtons zu Google Maps (+ schnelle Route)

        CameraUpdate cu3 = CameraUpdateFactory.newLatLngZoom(newMarkerOptions.getPosition(), 13);

    }

    public MarkerOptions getMarkerOptionsFromLatLng(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title("Sie befinden sich hier");
        //options.icon(BitmapDescriptorFactory.defaultMarker(
        //      BitmapDescriptorFactory.HUE_RED));
        options.icon(BitmapDescriptorFactory.defaultMarker());
        return options;
    }

    public void makeAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public static String[] getKategorienPlusBeschreibung() {
        String[] temp = new String[kategorien.length + 1];
        temp[0] = "Kategorien auswählen";
        for (int i = 0; i < kategorien.length; i++) {
            temp[i+1] = kategorien[i];
        }
        return temp;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //https://android.jlelse.eu/the-least-you-can-do-with-camera2-api-2971c8c81b8b
            case R.id.camera:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    camera_helper.initializeCamera();
                }
                break;
        }
    }
}
