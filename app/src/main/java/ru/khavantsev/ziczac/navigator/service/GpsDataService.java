package ru.khavantsev.ziczac.navigator.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class GpsDataService extends Service {

    public static final String LOG_TAG = GpsDataService.class.toString();
    public static final String LOCATION_BROADCAST_ACTION = "ru.khavantsev.ziczac.navigator.locationbroadcast";
    public static final String LOCATION_BROADCAST_EXTRA_NAME = "location";
    private boolean inWork = false;


    private LocationManager locationManager;
    private Location lastLocation;


    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        inWork = true;
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        inWork = false;
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    private void someTask() {

        new Thread(new Runnable() {
            public void run() {
                while (true){
                    if (!inWork) {
                        Log.d(LOG_TAG, "Stopped");
                        break;
                    }
                    try {
                        if(lastLocation != null){
                            Intent intent = new Intent(LOCATION_BROADCAST_ACTION);
                            intent.putExtra(LOCATION_BROADCAST_EXTRA_NAME, lastLocation);

                            GeomagneticField geoField = new GeomagneticField(
                                    Double.valueOf(lastLocation.getLatitude()).floatValue(),
                                    Double.valueOf(lastLocation.getLongitude()).floatValue(),
                                    Double.valueOf(lastLocation.getAltitude()).floatValue(),
                                    System.currentTimeMillis()
                            );
                            float declination = geoField.getDeclination();

                            sendBroadcast(intent);
                        }
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
