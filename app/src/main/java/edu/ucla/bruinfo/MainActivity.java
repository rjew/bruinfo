package edu.ucla.bruinfo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Throwables;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = MainActivity.class.getName();

    public static final String DATASTREAM_PREFS = "com.pubnub.example.android.datastream.mapexample.DATASTREAM_PREFS";
    public static final String DATASTREAM_UUID = "com.pubnub.example.android.datastream.mapexample.DATASTREAM_UUID";

    public static final String PUBLISH_KEY = "pub-c-a8c362d2-be78-4575-9a13-79db7816164f";
    public static final String SUBSCRIBE_KEY = "sub-c-1da9dc50-3417-11e7-81b3-02ee2ddab7fe";
    public static final String CHANNEL_NAME = "maps-channel";

    private GoogleMap mMap;

    private PubNub mPubNub;

    private SharedPreferences mSharedPrefs;

    private Marker mMarker;
    private Polyline mPolyline;

    private LocationHelper mLocationHelper;

    private List<LatLng> mPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = getSharedPreferences(DATASTREAM_PREFS, MODE_PRIVATE);
        if (!mSharedPrefs.contains(DATASTREAM_UUID)) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.mLocationHelper = new LocationHelper(this, getLocationListener());

        initPubNub();
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
        mMap.setMyLocationEnabled(true);
    }

    private final LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(final Location newLocation) {
                JSONObject location = new JSONObject();

                Log.i(TAG, String.valueOf(newLocation.getLatitude()));
                Log.i(TAG, String.valueOf(newLocation.getLongitude()));

/*                if (newLocation != null) {
                    location.tryPut("lat", String.valueOf(newLocation.getLatitude()));
                    location.tryPut("lon", String.valueOf(newLocation.getLongitude()));
                }

                MainActivity.this.mPubnub.setState(Constants.CHANNEL_NAME, MainActivity.this.mUsername, location, new Callback() {
                    @Override
                    public void successCallback(String channel, Object message) {
                        Log.v("setState", channel + ":" + message);
                        mPresenceMapAdapter.update(new PresenceMapPojo(mUsername, newLocation.getLatitude(), newLocation.getLongitude(), DateTimeUtil.getTimeStampUtc()));
                    }
                });*/
            }
        };
    }

    private final void initPubNub() {
        PNConfiguration config = new PNConfiguration();

        config.setPublishKey(PUBLISH_KEY);
        config.setSubscribeKey(SUBSCRIBE_KEY);
        config.setSecure(true);

        this.mPubNub = new PubNub(config);

        this.mPubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                // no status handler for simplicity
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                try {
                    Log.v(TAG, JsonUtil.asJson(message));

                    Map<String, String> map = JsonUtil.convert(message.getMessage(), LinkedHashMap.class);
                    String lat = map.get("lat");
                    String lng = map.get("lng");

                    updateLocation(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                // no presence handler for simplicity
            }
        });


        this.mPubNub.subscribe().channels(Arrays.asList(CHANNEL_NAME)).execute();
    }

    private void updateLocation(final LatLng location) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPoints.add(location);

                if (MainActivity.this.mMarker != null) {
                    MainActivity.this.mMarker.setPosition(location);
                } else {
                    MainActivity.this.mMarker = mMap.addMarker(new MarkerOptions().position(location));
                }

                if (MainActivity.this.mPolyline != null) {
                    MainActivity.this.mPolyline.setPoints(mPoints);
                } else {
                    MainActivity.this.mPolyline = mMap.addPolyline(new PolylineOptions().color(Color.BLUE).addAll(mPoints));
                }


                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

                Log.i(TAG, location.toString());
            }
        });
    }
}

