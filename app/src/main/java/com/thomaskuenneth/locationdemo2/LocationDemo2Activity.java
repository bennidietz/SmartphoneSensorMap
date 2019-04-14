package com.thomaskuenneth.locationdemo2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationDemo2Activity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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
            mMap.animateCamera(cu3, 5000, null);

        }
    }

    public Bitmap rescaleImageForIcon(int drawable_id) {
        final int width = 100;
        final int height = 100;
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), drawable_id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

}
