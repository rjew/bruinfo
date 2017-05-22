package edu.ucla.bruinfo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = MainActivity.class.getName();
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2017;
    private static final int REQUEST_APP_SETTINGS = 168;
    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyDCtM8cDa6Gj_I0jUG4dh8fihRRqmi0jHo";
    private final String RADIUS = "75"; //meters

    private GoogleMap mMap;

    private LocationHelper mLocationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.mLocationHelper = new LocationHelper(this, getLocationListener());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.mLocationHelper != null) {
            this.mLocationHelper.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.mLocationHelper != null) {
            this.mLocationHelper.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "Enable location services for accurate data", Toast.LENGTH_LONG).show();
                goToSettings();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else { //Permission already granted
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) throws SecurityException{
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted, yay!
                    mMap.setMyLocationEnabled(true);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Enable location services for accurate data", Toast.LENGTH_LONG).show();
                    goToSettings();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    private LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(final Location newLocation) {
                Log.i(TAG, String.valueOf(newLocation.getLatitude()));
                Log.i(TAG, String.valueOf(newLocation.getLongitude()));

                LatLng latLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                mMap.animateCamera(cameraUpdate);

                new LocationsGrabber(newLocation).execute();
            }
        };
    }

    private class LocationsGrabber extends AsyncTask<Void, Void, Void> implements
            GoogleMap.OnMarkerClickListener {
        private Location location;

        private LocationsGrabber(Location location) {
            this.location = location;
        }

        /** Called when the user clicks a marker. */
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.i(TAG, "Marker clicked!");

            // Return false to indicate that we have not consumed the event and that we wish
            // for the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            return false;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            JSONObject json = readJsonFromUrl(generateURL(this.location));

            try {
                JSONArray locationResults = json.getJSONArray("results");

                for (int i = 0; i < locationResults.length(); i++) {
                    final String locationName = locationResults.getJSONObject(i).getString("name");
                    Log.i(TAG, locationName);

                    final String vicinity = locationResults.getJSONObject(i).getString("vicinity");

                    JSONObject location = locationResults.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                    final double latitude = location.getDouble("lat");
                    final double longitude = location.getDouble("lng");
                    Log.i(TAG, Double.toString(latitude));
                    Log.i(TAG, Double.toString(longitude));

                    final LocationsGrabber self = this;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(locationName)
                                    .snippet(vicinity));

                            // Set a listener for marker click.
                            mMap.setOnMarkerClickListener(self);
                        }
                    });

                    //Example of json response
                    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=34.0750228,-118.4418203&radius=75&key=AIzaSyDCtM8cDa6Gj_I0jUG4dh8fihRRqmi0jHo
                }
            } catch (JSONException ex) {
                Log.e(TAG, "\nERROR in doInBackground - JSONException: " + ex.toString());
                System.exit(1);
            }

            return null;
        }
    }

    private String generateURL(Location location) {
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location="
                + String.valueOf(location.getLatitude())
                + "," + String.valueOf(location.getLongitude())
                + "&radius="
                + RADIUS
                //+ "type="
                //+ "point_of_interest"
                + "&key="
                + GOOGLE_PLACES_API_KEY;

        return URL.replace(" ", "%20");
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
                String jsonText = readAll(rd);
                return new JSONObject(jsonText);
            } catch (JSONException ex) {
                Log.e(TAG, "\nERROR in readJsonFromUrl - JSONException: " + ex.toString());
                System.exit(1);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, "\nERROR in readJsonFromUrl - IOException: " + ex.toString());
            System.exit(1);
        }

        return new JSONObject();
    }
}

