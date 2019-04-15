package com.thomaskuenneth.locationdemo2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.Map;

public class LocationDemo2Activity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;

    private GoogleMap mMap;

    Context context = this;
    DBHandler dbHandler;
    HashMap<Marker, String> markers;
    Button gotoFeature;
    String currentidentifier = "";

    // navigation bars
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    Button aktualisieren;
    ArrayList<StateVO> kategorienChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dbHandler = new DBHandler(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        markers = new HashMap<>();
        gotoFeature = findViewById(R.id.gotoFeature);
        gotoFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoFeature = new Intent(context, ShowFeature.class);
                gotoFeature.putExtra(DBHandler.OVERALL_IDENTIFIER, currentidentifier);
                startActivity(gotoFeature);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        //ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        //params.height = (getResources().getDisplayMetrics().heightPixels)*8/10;
        //mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        nv = (NavigationView)findViewById(R.id.nv);
        View headerView = nv.getHeaderView(0);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.account:
                        Toast.makeText(context, "My Account",Toast.LENGTH_SHORT).show();
                    case R.id.settings:
                        Toast.makeText(context, "Settings",Toast.LENGTH_SHORT).show();
                    case R.id.mycart:
                        Toast.makeText(context, "My Cart",Toast.LENGTH_SHORT).show();
                    default:
                        return true;
                }
            }
        });
        //TextView navUsername = (TextView) headerView.findViewById(R.id.navUsername); => get view from navigation_menu.xml (items)
        //View headerLayout = nv.inflateHeaderView(R.layout.nav_header); // get views from nav_header.xml
        final Spinner klasse = headerView.findViewById(R.id.klasse);
        kategorienChecked = new ArrayList<>();
        for (int i = 0; i < AddMarker.getKategorienPlusBeschreibung().length; i++) {
            StateVO stateVO = new StateVO();
            stateVO.setTitle(AddMarker.getKategorienPlusBeschreibung()[i]);
            stateVO.setSelected(false);
            kategorienChecked.add(stateVO);
        }
        MyAdapter myAdapter = new MyAdapter(context, 0,
                kategorienChecked);
        klasse.setAdapter(myAdapter);

        aktualisieren = headerView.findViewById(R.id.aktualisieren);
        aktualisieren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Marker> markerstoremove = new ArrayList<>();
                HashMap<Marker, String> markerstoadd = new HashMap<>();
                for (Marker marker: markers.keySet()) {
                    marker.remove();
                    for (StateVO kategorie: kategorienChecked) {
                        if (kategorie.isSelected() && marker.getSnippet().equals(kategorie.getTitle())) {
                            MarkerOptions options = new MarkerOptions();
                            options.position(marker.getPosition()).title(marker.getTitle()).snippet(marker.getSnippet());
                            //options.icon(BitmapDescriptorFactory.defaultMarker(
                            //      BitmapDescriptorFactory.HUE_RED));
                            options.icon(BitmapDescriptorFactory.defaultMarker(getColorForClass(marker.getSnippet())));
                            String identifier = markers.get(marker);
                            markerstoremove.add(marker);
                            //markers.remove(marker);
                            markerstoadd.put(mMap.addMarker(options), identifier);
                            //markers.put(mMap.addMarker(options), identifier);

                        }
                    }
                }
                for (Marker marker: markerstoremove) {
                    markers.remove(marker);
                }
                for (Marker marker: markerstoadd.keySet()) {
                    markers.put(marker, markerstoadd.get(marker));
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if ((requestCode == PERMISSIONS_ACCESS_FINE_LOCATION) &&
                (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED)) {
            markerDemo();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item)) {
            return true;
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;
                for (Marker marker1: markers.keySet()) {
                    if (marker1.equals(marker)) {
                        ArrayList<Map<String, String>> records = dbHandler.executeSQLToDict(context, DBHandler.table1_keys, "select * from " + DBHandler.table1_keys[0] +
                                " where " + DBHandler.OVERALL_IDENTIFIER + " = " + markers.get(marker1));
                        if (records.size() > 0) {
                            //Toast.makeText(context, records.get(0).get(DBHandler.OVERALL_CLASS), Toast.LENGTH_LONG).show();
                            gotoFeature.setVisibility(View.VISIBLE);
                            gotoFeature.setText(records.get(0).get(DBHandler.TABLE1_C1) + " anzeigen ->");
                            currentidentifier = markers.get(marker1);
                        }
                    }
                }
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                gotoFeature.setVisibility(View.INVISIBLE);
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
        addAllMarker();

    }

    private void markerDemo() throws SecurityException {
        MarkerOptions options = new MarkerOptions();
        LocationManager m = getSystemService(LocationManager.class);
        if (m == null) {
            return;
        }
        Location loc =
                m.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc != null) {
            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
            options.position(pos).title("Sie befinden sich hier");
            //options.icon(BitmapDescriptorFactory.defaultMarker(
              //      BitmapDescriptorFactory.HUE_RED));
            options.icon(BitmapDescriptorFactory.fromBitmap(rescaleImageForIcon(R.drawable.mobile_phone)));
            mMap.addMarker(options);
            mMap.getUiSettings().setZoomControlsEnabled(true); // Kontrollelemente für Benutzer anzegien
            mMap.getUiSettings().setCompassEnabled(true); // zeige den Kompass an (wenn Karte gedreht)
            mMap.getUiSettings().setMapToolbarEnabled(true); // kontextabhängige Werkzeugleiste -> ImageButtons zu Google Maps (+ schnelle Route)

            LatLng berlin = new LatLng(
                    Location.convert("52:31:12"),
                    Location.convert("13:24:36"));
            CameraUpdate cu1 = CameraUpdateFactory.newLatLngZoom(berlin, 15);
            mMap.moveCamera(cu1);
            CameraUpdate cu3 = CameraUpdateFactory.newLatLng(pos);
            mMap.moveCamera(cu3);

        }
    }

    public void addAllMarker() {
        ArrayList<Map<String, String>> allrecords = dbHandler.executeSQLToDict(context, DBHandler.table1_keys, "select * from " + DBHandler.table1_keys[0] + " where " + DBHandler.OVERALL_LATITUDE  +
                " is not null and " + DBHandler.OVERALL_LONGITUDE + " is not null");
        for (Map<String, String> marker: allrecords) {
            LatLng latLng = new LatLng(Double.parseDouble(marker.get(DBHandler.OVERALL_LATITUDE)), Double.parseDouble(marker.get(DBHandler.OVERALL_LONGITUDE)));
            addMarker(getMarkerOptionsFromLatLng(latLng, marker.get(DBHandler.TABLE1_C1), marker.get(DBHandler.OVERALL_CLASS)), marker.get(DBHandler.OVERALL_IDENTIFIER));
        }
    }

    public MarkerOptions getMarkerOptionsFromLatLng(LatLng latLng, String name, String klasse) {
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title(name).snippet(klasse);
        //options.icon(BitmapDescriptorFactory.defaultMarker(
        //      BitmapDescriptorFactory.HUE_RED));
        options.icon(BitmapDescriptorFactory.defaultMarker(getColorForClass(klasse)));
        return options;
    }

    private float getColorForClass(String klasse) {
        String[] kategorien = AddMarker.kategorien;
        if (klasse.equals(kategorien[0])) {
            // Mode
            return BitmapDescriptorFactory.HUE_GREEN;
        } else if (klasse.equals(kategorien[1])) {
            // Dekroation/ Einrichtung
            return BitmapDescriptorFactory.HUE_AZURE;
        } else if (klasse.equals(kategorien[2])) {
            // Kunst
            return BitmapDescriptorFactory.HUE_RED;
        } else if (klasse.equals(kategorien[3])) {
            // Kulinarisches
            return BitmapDescriptorFactory.HUE_YELLOW;
        } else {
            // andere
            return BitmapDescriptorFactory.HUE_BLUE;
        }
    }

    public void addMarker(MarkerOptions newMarkerOptions, String identifier) {
        Marker marker = mMap.addMarker(newMarkerOptions);
        markers.put(marker, identifier);
    }

    public Bitmap rescaleImageForIcon(int drawable_id) {
        final int width = 100;
        final int height = 100;
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), drawable_id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


}
