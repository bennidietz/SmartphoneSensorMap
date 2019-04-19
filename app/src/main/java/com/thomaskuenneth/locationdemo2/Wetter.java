package com.thomaskuenneth.locationdemo2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Wetter extends AppCompatActivity implements OnMapReadyCallback {


    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    Marker marker;
    Context context = this;


    private static final int PERMISSIONS_ACCESS_FINE_LOCATION
            = 0x1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wetter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
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
        marker.showInfoWindow();
        mMap.getUiSettings().setZoomControlsEnabled(true); // Kontrollelemente für Benutzer anzegien
        mMap.getUiSettings().setCompassEnabled(true); // zeige den Kompass an (wenn Karte gedreht)
        mMap.getUiSettings().setMapToolbarEnabled(true); // kontextabhängige Werkzeugleiste -> ImageButtons zu Google Maps (+ schnelle Route)
        CameraUpdate cu3 = CameraUpdateFactory.newLatLngZoom(newMarkerOptions.getPosition(), 13);

    }

    public MarkerOptions getMarkerOptionsFromLatLng(final LatLng latLng) {
        final MarkerOptions options = new MarkerOptions();
        options.position(latLng).title("Sie befinden sich hier").snippet(getWeatherOfLatLng(latLng));
        //options.icon(BitmapDescriptorFactory.defaultMarker(
        //      BitmapDescriptorFactory.HUE_RED));
        options.icon(BitmapDescriptorFactory.defaultMarker());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        System.out.println("iudc " + getWeatherOfLatLng(latLng));
        final String targeturl = "http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&APPID=7f943fcef3f26af302ac9ad6597082c9&units=metric";

        try {
            System.out.println("iudc " + getJSONObjectFromURL(targeturl).toString());
            JSONObject response = getJSONObjectFromURL(targeturl);
            JSONObject wetter = ((JSONObject)(response.getJSONArray("weather").get(0)));
            String description = wetter.getString("description");
            String icon_id = wetter.getString("icon");

            JSONObject main = response.getJSONObject("main");
            double temperature = main.getDouble("temp");
            int luftfeuchte = main.getInt("humidity");
            String info = "Beschreibung: " + description + "\n" +
                    "Temperatur: " + temperature + " °C" + "\n" +
                    "Luftfeuchte: " + luftfeuchte + " %";
            options.snippet(info);
            String icon_url = "http://openweathermap.org/img/w/" + icon_id + ".png";
            options.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(getBitmapFromURL(icon_url),100, 100, false)));
            Address address = latLongToAddress(latLng);
            if (address != null) {
                options.title(address.getAddressLine(0).replace(",", ",\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //StrictMode.setThreadPolicy(policy);
        System.out.println("iuvx " + latLng.latitude);
        System.out.println("iuvx " + latLng.longitude);



        System.out.println("oucxy " + getWeatherOfLatLng(latLng));
        return options;
    }

    public void javascriptt(LatLng latLng) {
        WebView browser = new WebView(this);
        browser.getSettings().setJavaScriptEnabled(true); //Yes you have to do it
        browser.loadUrl("http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&APPID=7f943fcef3f26af302ac9ad6597082c9"); //If you put the HTML file in asset folder of android
        browser.evaluateJavascript(
                "(function() { var xmlHttp = new XMLHttpRequest(); xmlHttp.open( \"GET\", \"https://stackoverflow.com/questions/247483/http-get-request-in-javascript\", false ); xmlHttp.send( null ); return xmlHttp.responseText; })();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String html) {
                        Log.d("HTML", html);
                        System.out.println("ioscfuhro" + html);
                        // code here
                    }
                });
    }


    public String getWeatherOfLatLng(LatLng latLng) {
        URL url = null;
        InputStream in = null;
        try {
            url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&APPID=7f943fcef3f26af302ac9ad6597082c9");
            System.out.println(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return convertStreamToString(in);
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        if (is == null) return "";
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);
        urlConnection.disconnect();
        return new JSONObject(jsonString);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public Address latLongToAddress(LatLng latLng) {
        Geocoder geoCoder = new Geocoder(context);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
            return bestMatch;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
