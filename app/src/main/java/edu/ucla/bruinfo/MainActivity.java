package edu.ucla.bruinfo;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = MainActivity.class.getName();

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //if (getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION")
        //        == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        //}
    }

    private final LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(final Location newLocation) {
                Log.i(TAG, String.valueOf(newLocation.getLatitude()));
                Log.i(TAG, String.valueOf(newLocation.getLongitude()));

                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                //mMap.animateCamera(cameraUpdate);
            }
        };
    }
}

