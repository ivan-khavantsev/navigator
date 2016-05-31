package ru.khavantsev.ziczac.navigator.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import java.util.concurrent.TimeUnit;

public class GpsDataService extends Service {

    public static final String LOCATION_BROADCAST_ACTION = "ru.khavantsev.ziczac.navigator.locationbroadcast";
    public static final String LOCATION_BROADCAST_EXTRA_LOCATION = "location";
    public static final String LOCATION_BROADCAST_EXTRA_DECLINATION = "declination";
    private static final float GPS_MIN_DISTANCE = 5;
    private static final long GPS_MIN_TIME = 1000 * 1; //Millisecond
    private static final long BROADCAST_PERIOD = 1; //Second

    private boolean inWork = false;

    private LocationManager locationManager;
    private Location location;


    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            GpsDataService.this.location = location;
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, locationListener);
        //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
                while (true) {
                    if (!inWork) {
                        break;
                    }
                    try {
                        if (location != null) {
                            Intent intent = new Intent(LOCATION_BROADCAST_ACTION);
                            intent.putExtra(LOCATION_BROADCAST_EXTRA_LOCATION, location);

                            GeomagneticField geoField = new GeomagneticField(
                                    Double.valueOf(location.getLatitude()).floatValue(),
                                    Double.valueOf(location.getLongitude()).floatValue(),
                                    Double.valueOf(location.getAltitude()).floatValue(),
                                    System.currentTimeMillis()
                            );
                            float declination = geoField.getDeclination();
                            intent.putExtra(LOCATION_BROADCAST_EXTRA_DECLINATION, declination);

                            sendBroadcast(intent);
                        }
                        TimeUnit.SECONDS.sleep(BROADCAST_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
