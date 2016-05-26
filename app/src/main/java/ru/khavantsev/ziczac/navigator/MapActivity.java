package ru.khavantsev.ziczac.navigator;

import android.content.*;
import android.hardware.GeomagneticField;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MapActivity extends AppCompatActivity {

    public static final String LOG_TAG = MapActivity.class.toString();

    private SharedPreferences sp;
    private BroadcastReceiver br;

    private TextView tvLocationGPS;
    private TextView pointData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        pointData = (TextView) findViewById(R.id.pointData);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().clear().apply(); // DEBUG

        startService(new Intent(this, GpsDataService.class));

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Location location = intent.getParcelableExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_NAME);
                showLocation(location);
                Log.d(LOG_TAG, location.toString());
            }
        };

    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, GpsDataService.class));
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location_settings:
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;
            case R.id.menu_points:
                startActivity(new Intent(this, PointsActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        registerReceiver(br, new IntentFilter(GpsDataService.LOCATION_BROADCAST_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(br);
        super.onPause();
    }

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

            LatLon myPosition = new LatLon(location.getLatitude(), location.getLongitude());
            LatLon point = new LatLon(55.755833, 37.617778);

            long rhumbDistance = GeoCalc.toRealDistance(GeoCalc.rhumbDistance(myPosition, point));

            double rhumbAzimuth = GeoCalc.toRealAzimuth(GeoCalc.rhumbAzimuth(myPosition, point));


            GeomagneticField geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    System.currentTimeMillis()
            );

            float declination = geoField.getDeclination();

            pointData.setText("Distance: " + rhumbDistance + ", Azimuth: " + rhumbAzimuth + ", Declination: " + declination);

            tvLocationGPS.setText(formatLocation(location));
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
}
