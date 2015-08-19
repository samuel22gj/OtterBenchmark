package com.otter.otterbenchmark.gps;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.otter.otterbenchmark.ErrorDialog;
import com.otter.otterbenchmark.R;
import com.otter.otterbenchmark.Util;

public class GpsTest extends AppCompatActivity
        implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {
    private static final String TAG = GpsTest.class.getSimpleName();

    private static final long MIN_UPDATE_INTERVAL  = 0L; // milliseconds
    private static final float MIN_UPDATE_DISTANCE = 0F; // meters

    private LocationManager mLocationManager;

    private Button location_settings;

    private TextView gps_status;
    private TextView network_status;
    private TextView passive_status;
    private TextView provider;
    private TextView accuracy;
    private TextView longitude;
    private TextView latitude;
    private TextView bearing;
    private TextView altitude;
    private TextView speed;
    private TextView time;
    private TextView max_satellites;
    private TextView satellites;
    private TextView first_fix_time;
    private TextView time_stamp;
    private TextView nmea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gps_test);
        findViews();
        initButtons();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            // Show not support location service dialog to close activity.
            ErrorDialog errorDialog = ErrorDialog.newInstance(
                    getString(R.string.gps_not_support_location_service));
            errorDialog.show(getFragmentManager(), ErrorDialog.FRAGMENT_TAG);
            return;
        }

        registerGpsListeners();

        showProvidersInfo();

        String bestProvider = mLocationManager.getBestProvider(new Criteria(), false);
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(bestProvider);
        if (lastKnownLocation != null) showLocationInfo(lastKnownLocation);

        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
        if (gpsStatus != null) showGpsStatusInfo(gpsStatus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLocationManager != null) {
            unregisterGpsListeners();
        }
    }

    private void findViews() {
        location_settings = (Button) findViewById(R.id.location_settings);
        gps_status = (TextView) findViewById(R.id.gps_status);
        network_status = (TextView) findViewById(R.id.network_status);
        passive_status = (TextView) findViewById(R.id.passive_status);
        provider = (TextView) findViewById(R.id.provider);
        accuracy = (TextView) findViewById(R.id.accuracy);
        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        bearing = (TextView) findViewById(R.id.bearing);
        altitude = (TextView) findViewById(R.id.altitude);
        speed = (TextView) findViewById(R.id.speed);
        time = (TextView) findViewById(R.id.time);
        max_satellites = (TextView) findViewById(R.id.max_satellites);
        satellites = (TextView) findViewById(R.id.satellites);
        first_fix_time = (TextView) findViewById(R.id.first_fix_time);
        time_stamp = (TextView) findViewById(R.id.time_stamp);
        nmea = (TextView) findViewById(R.id.nmea);
    }

    private void initButtons() {
        location_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    private void registerGpsListeners() {
        // Register the listener with the Location Manager to receive location updates.
        if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "Register GPS Location Provider");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_UPDATE_INTERVAL, MIN_UPDATE_DISTANCE, this);
        }
        if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            Log.i(TAG, "Register Network Location Provider");
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_UPDATE_INTERVAL, MIN_UPDATE_DISTANCE, this);
        }

        // Register the listener to receiving notifications when GPS status has changed.
        Log.i(TAG, "Register GpsStatusListener: " + mLocationManager.addGpsStatusListener(this));

        // Register the listener to receiving NMEA sentences from the GPS.
        Log.i(TAG, "Register NmeaListener: " + mLocationManager.addNmeaListener(this));
    }

    private void unregisterGpsListeners() {
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeNmeaListener(this);
    }

    private void showProvidersInfo() {
        gps_status.setText(getString(
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? R.string.enable : R.string.disable));
        network_status.setText(getString(
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ? R.string.enable : R.string.disable));
        passive_status.setText(getString(
                mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) ? R.string.enable : R.string.disable));
    }

    private void showLocationInfo(Location location) {
        provider.setText(location.getProvider());
        accuracy.setText(String.valueOf(location.getAccuracy()));
        longitude.setText(String.valueOf(location.getLongitude()));
        latitude.setText(String.valueOf(location.getLatitude()));
        bearing.setText(String.valueOf(location.getBearing()));
        altitude.setText(String.valueOf(location.getAltitude()));
        speed.setText(String.valueOf(location.getSpeed()));
        time.setText(Util.convertMillisecondToDateTime(location.getTime()));
    }

    private void showGpsStatusInfo(GpsStatus gpsStatus) {
        max_satellites.setText(String.valueOf(gpsStatus.getMaxSatellites()));
        int satelliteCount = 0;
        for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
            // TODO: Show each GpsSatellite detail.
            satelliteCount++;
        }
        satellites.setText(String.valueOf(satelliteCount));
        first_fix_time.setText(String.valueOf(gpsStatus.getTimeToFirstFix()));
    }

    private void showNmeaInfo(long timestamp, String nmea) {
        time_stamp.setText(Util.convertMillisecondToDateTime(timestamp));
        this.nmea.setText(nmea);
    }

    /* **************** *
     * LocationListener *
     * **************** */
    @Override
    public void onLocationChanged(Location location) {
//        Log.v(TAG, "onLocationChanged");

        showLocationInfo(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusStr = null;
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                statusStr = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statusStr = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                statusStr = "AVAILABLE";
                break;
        }
        Log.v(TAG, "onStatusChange(" + provider + ", " + statusStr + ")");
    }

    @Override
    public void onProviderEnabled(String provider) {
//        Log.v(TAG, "onProviderEnabled: " + provider);

        showProvidersInfo();
    }

    @Override
    public void onProviderDisabled(String provider) {
//        Log.v(TAG, "onProviderDisabled: " + provider);

        showProvidersInfo();
    }

    /* ****************** *
     * GpsStatus.Listener *
     * ****************** */
    @Override
    public void onGpsStatusChanged(int event) {
        String eventStr = null;
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                eventStr = "GPS_EVENT_STARTED";
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                eventStr = "GPS_EVENT_STOPPED";
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                eventStr = "GPS_EVENT_FIRST_FIX";
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                eventStr = "GPS_EVENT_SATELLITE_STATUS";
                break;
        }
        Log.v(TAG, "onGpsStatusChanged("+ eventStr + ")");

        showGpsStatusInfo(mLocationManager.getGpsStatus(null));
    }

    /* ********************** *
     * GpsStatus.NmeaListener *
     * ********************** */
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
//        Log.v(TAG, "onNmeaReceived: " + timestamp + ", " + nmea);

        showNmeaInfo(timestamp, nmea);
    }
}
