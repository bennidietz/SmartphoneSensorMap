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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationDemo2Activity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;

    private GoogleMap mMap;

    Context context = this;
    DBHandler dbHandler;
    HashMap<Marker, JSONObject> markers;
    Button gotoFeature;
    JSONObject currentidentifier = null;

    CheckBox verkehr;
    CheckBox satellit;

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
                Intent showFeature = new Intent(context, ShowFeature.class);
                ShowFeature.jsonObject = currentidentifier;
                startActivity(showFeature);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        //ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        //params.height = (getResources().getDisplayMetrics().heightPixels)*8/10;
        //mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);
        verkehr = findViewById(R.id.verkehr);
        verkehr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mMap.setTrafficEnabled(true);
                } else {
                    mMap.setTrafficEnabled(false);
                }
            }
        });
        satellit = findViewById(R.id.satellite);
        satellit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    satellit.setButtonTintList(ContextCompat.getColorStateList(context, R.color.gelb));
                    satellit.setTextColor(getColor(R.color.gelb));
                    verkehr.setButtonTintList(ContextCompat.getColorStateList(context, R.color.gelb));
                    verkehr.setTextColor(getColor(R.color.gelb));
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    satellit.setButtonTintList(ContextCompat.getColorStateList(context, R.color.blau));
                    satellit.setTextColor(getColor(R.color.blau));
                    verkehr.setButtonTintList(ContextCompat.getColorStateList(context, R.color.blau));
                    verkehr.setTextColor(getColor(R.color.blau));
                }
            }
        });

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
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        Toast.makeText(context, "My Account",Toast.LENGTH_SHORT).show();
                    case R.id.settings:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        Toast.makeText(context, "Settings",Toast.LENGTH_SHORT).show();
                    case R.id.mycart:
                        mMap.setIndoorEnabled(true);
                        mMap.setTrafficEnabled(true);
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
                HashMap<Marker, JSONObject> markerstoadd = new HashMap<>();
                for (Marker marker: markers.keySet()) {
                    marker.remove();
                    for (StateVO kategorie: kategorienChecked) {
                        if (kategorie.isSelected() && marker.getSnippet().equals(kategorie.getTitle())) {
                            MarkerOptions options = new MarkerOptions();
                            options.position(marker.getPosition()).title(marker.getTitle()).snippet(marker.getSnippet());
                            //options.icon(BitmapDescriptorFactory.defaultMarker(
                            //      BitmapDescriptorFactory.HUE_RED));
                            options.icon(BitmapDescriptorFactory.defaultMarker(getColorForClass(marker.getSnippet())));
                            JSONObject identifier = markers.get(marker);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setIndoorEnabled(true);
        //mMap.setTrafficEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (Marker marker1: markers.keySet()) {
                    if (marker1.equals(marker)) {
                        gotoFeature.setVisibility(View.VISIBLE);
                        currentidentifier = markers.get(marker1);
                        gotoFeature.setText(getSafeProperty(currentidentifier, "Name") + " anzeigen ->");
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
        //addAllMarker();
        addMarkerFromGoogleSheets();

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

    private void addMarkerFromGoogleSheets() {
        String url = "https://script.googleusercontent.com/macros/echo?user_content_key=BLdlerX3HiG9IEkTXb3FvviFUhsuY-lgoapxXv7QD6bLPjNpnG2mNrC68qomxQ6gOgO4-uWcFKspdMLyEv7zfg5gKUrfRPAEOJmA1Yb3SEsKFZqtv3DaNYcMrmhZHmUMWojr9NvTBuBLhyHCd5hHa1GhPSVukpSQTydEwAEXFXgt_wltjJcH3XHUaaPC1fv5o9XyvOto09QuWI89K6KjOu0SP2F-BdwUjuvBaj8HFII3A9hjDsF1qONghHQbkTx2aqgQmKiPht7UeAqicXBN8fIuKZ-5fiJXTsHi7XeTbZLcFdVIoNMa9A&lib=MnrE7b2I2PjfH799VodkCPiQjIVyBAxva";
        JSONObject jsonObject = null;
        try {
            jsonObject = Wetter.getJSONObjectFromURL(url);
            System.out.println(jsonObject.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("Lokale");
            System.out.println("iuc " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject element = jsonArray.getJSONObject(i);
                String name = getSafeProperty(element, "Name");
                String beschreibung = getSafeProperty(element, "Beschreibung");
                String kategorie = getSafeProperty(element, "Kategorie");
                String koordinaten = getSafeProperty(element, "Koordinaten");
                String bilder = getSafeProperty(element, "Bilder");
                System.out.println("iuc " + name);
                LatLng latLng = decodeCoordinates(koordinaten);
                if (latLng == null) {
                    break;
                }
                addMarker(getMarkerOptionsFromLatLng(latLng,name,kategorie), element);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(getUserReadableError(e));
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println(getUserReadableError(e));
        }
    }

    public static LatLng decodeCoordinates(String savedCoordinates) {
        if (savedCoordinates.contains(",") && savedCoordinates.split(",").length > 1) {
            String[] coors = savedCoordinates.split(",");
            Double longitude = Double.parseDouble(coors[0].replace(" ", ""));
            Double latitidue = Double.parseDouble(coors[1].replace(" ", ""));
            LatLng latLng = new LatLng(longitude, latitidue);
            return latLng;
        }
        return null;
    }

    public static String getSafeProperty(JSONObject jsonObject, String property) {
        if (jsonObject.has(property)) {
            try {
                return jsonObject.getString(property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
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

    public void addMarker(MarkerOptions newMarkerOptions, JSONObject identifier) {
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

    public static String getUserReadableError(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }


}
