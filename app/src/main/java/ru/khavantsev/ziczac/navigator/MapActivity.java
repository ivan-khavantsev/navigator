package ru.khavantsev.ziczac.navigator;

import android.hardware.GeomagneticField;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

public class MapActivity extends AppCompatActivity {

    public static final double pi = 3.14159265358979;
    public static final double earthRadius = 6372795;
    public static final int accuracy = 7;

    static class DistanceAzimuth {
        public double distance;
        public double azimuth;
    }

    public static double asAngle(double value) {
        return (value * 180) / pi;
    }

    public static double asRadians(double value) {
        return value * pi / 180;
    }


    public static DistanceAzimuth calc(double lat1, double lon1, double lat2, double lon2) {

        //Получение в радианах
        double radLat1 = lat1 * pi / 180;
        double radLat2 = lat2 * pi / 180;
        double radLon1 = lon1 * pi / 180;
        double radLon2 = lon2 * pi / 180;


        //косинусы и синусы широт и разниц долгот
        double cosLat1 = Math.cos(radLat1);
        double cosLat2 = Math.cos(radLat2);
        double sinLat1 = Math.sin(radLat1);
        double sinLat2 = Math.sin(radLat2);
        double delta = radLon2 - radLon1;
        double cosDelta = Math.cos(delta);
        double sinDelta = Math.sin(delta);


        //вычисления длины большого круга

        double p1 = Math.pow(cosLat2 * sinDelta, 2);
        double p2 = Math.pow(((cosLat1 * sinLat2) - (sinLat1 * cosLat2 * cosDelta)), 2);
        double p3 = Math.pow((p1 + p2), 0.5);
        double p4 = sinLat1 * sinLat2;
        double p5 = cosLat1 * cosLat2 * cosDelta;
        double p6 = p4 + p5;
        double p7 = p3 / p6;
        double angleRad = Math.atan(p7);
        double dist = angleRad * earthRadius;

        //вычисление начального азимута
        double x = (cosLat1 * sinLat2) - (sinLat1 * cosLat2 * cosDelta);
        double y = sinDelta * cosLat2;
        double z = Math.toDegrees(Math.atan(-y / x));

        if (x < 0) {
            z = z + 180;
        }


        z = Math.toRadians(-(z + 180 % 360 - 180));
        double anglerad2 = z - ((2 * pi) * (Math.floor(z / (2 * pi))));
        double angledeg = (anglerad2 * 180) / pi;


        DistanceAzimuth di = new DistanceAzimuth();
        di.distance = dist;
        di.azimuth = angledeg;
        return di;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        pointData = (TextView) findViewById(R.id.pointData);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    TextView pointData;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

            DistanceAzimuth di = calc(location.getLatitude(), location.getLongitude(), 55.755833,37.617778);

            GeomagneticField geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    System.currentTimeMillis()
            );

            float declination = geoField.getDeclination();

            pointData.setText("Distance: " + di.distance + ", Azimuth: " + di.azimuth + ", Declination: " + declination);

            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    ;

}
