package com.thomaskuenneth.locationdemo2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ShowFeature extends AppCompatActivity implements OnMapReadyCallback {

    DBHandler dbHandler;
    Context context = this;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;
    Button route;
    LinearLayout linLayout, imageSlider;
    TextView beschreibung;

    static JSONObject jsonObject = null;

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
        beschreibung = findViewById(R.id.beschreibung);
        imageSlider = findViewById(R.id.imageSlider);
        linLayout = findViewById(R.id.linLayout);
        route = findViewById(R.id.route);
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = getLatLng();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + latLng.latitude + "," +
                                latLng.longitude));
                startActivity(intent);
            }
        });
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = (getResources().getDisplayMetrics().heightPixels)*35/100;
        mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setTitle(getName());
        getSupportActionBar().setSubtitle(getKategorie());

        if (!getBeschreibung().isEmpty()) {
            beschreibung.setText(getBeschreibung());
        }

        if (!getBildUrl().isEmpty()) {
            int height = getResources().getDisplayMetrics().heightPixels / 4;
            ImageView imageView = new ImageView(this);
            imageView.setPadding(10,0,10,0);
            Picasso.with(this)
                    .load(getBildUrl())
                    .into(imageView);
            imageSlider.addView(imageView);
        }

/*
            ArrayList<Bitmap> bilder = getPictures();
            if (bilder.size() > 0) {
                int height = getResources().getDisplayMetrics().heightPixels / 4;
                for (Bitmap bitmap: bilder) {
                    ImageView imageView = new ImageView(this);
                    imageView.setPadding(10,0,10,0);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int)(height/1.33333333333), height, true);
                    imageView.setImageBitmap(resizedBitmap);
                    imageSlider.addView(imageView);
                }
            }
            int i = 0;
        System.out.println("oucg " + jsonObject.toString());*/

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
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        LatLng latLng = getLatLng();
        addMarkerToMap(latLng);

    }


    public void addMarkerToMap(LatLng latLng) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title(getName());
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

    public static LatLng getLatLng() {
        return LocationDemo2Activity.decodeCoordinates(getProperty("Koordinaten"));
    }

    public static String getName() {
        return getProperty("Name");
    }

    public static String getBeschreibung() {
        return getProperty("Beschreibung");
    }

    public static String getKategorie() {
        return getProperty("Kategorie");
    }

    public static String getBildUrl() {
        return getProperty("Bilder");
    }

    public static String getProperty(String property) {
        return LocationDemo2Activity.getSafeProperty(jsonObject, property);
    }
}
