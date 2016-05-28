package ru.khavantsev.ziczac.navigator.activity;

import android.content.*;
import android.hardware.GeomagneticField;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.geo.GeoCalc;
import ru.khavantsev.ziczac.navigator.geo.LatLon;
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

import java.util.Date;

public class MapActivity extends AppCompatActivity {

    public static final String LOG_TAG = MapActivity.class.toString();

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        startService(new Intent(this, GpsDataService.class));
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

}
