package ru.khavantsev.ziczac.navigator.activity;

import android.content.*;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.adapter.PointsAdapter;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.dialog.PointEditDialog;
import ru.khavantsev.ziczac.navigator.dialog.ProjectionDialog;
import ru.khavantsev.ziczac.navigator.geo.GeoCalc;
import ru.khavantsev.ziczac.navigator.geo.LatLon;
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

import java.math.BigDecimal;
import java.util.*;

public class PointsActivity extends AppCompatActivity implements PointListener {

    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;

    private static final String ATTRIBUTE_AZIMUTH = "azimuth";
    private static final String ATTRIBUTE_DISTANCE = "distance";

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private static final String POINT_DIALOG_TAG = "Point";
    private static final String PROJECTION_DIALOG_TAG = "Projection";
    private ListView lvPoints;
    private PointsAdapter pointsAdapter;
    private PointService pointService;
    private ArrayList<Map<String, Object>> data;
    private Map<Long, Integer> pointIdListIndexMap;
    private SharedPreferences sp;
    private BroadcastReceiver br;
    private Location lastLocation;
    private float lastDeclination;
    private boolean usingDeclination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_points);

        lvPoints = findViewById(R.id.lvPoints);
        registerForContextMenu(lvPoints);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                lastLocation = intent.getParcelableExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_LOCATION);
                lastDeclination = intent.getFloatExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_DECLINATION, 0);
                LatLon selfLatLon = new LatLon(lastLocation.getLatitude(), lastLocation.getLongitude());
                if (data != null) {
                    for (int i = 0; i < data.size(); i++) {
                        try {
                            Map<String, Object> pointItem = data.get(i);

                            LatLon pointLatLon = new LatLon();
                            pointLatLon.latitude = Double.parseDouble((String) pointItem.get(PointService.ATTRIBUTE_NAME_LAT));
                            pointLatLon.longitude = Double.parseDouble((String) pointItem.get(PointService.ATTRIBUTE_NAME_LON));

                            float usesDeclination = (usingDeclination) ? lastDeclination : 0; // If settings set use declination else not use
                            double azimuth = GeoCalc.toRealAzimuth(GeoCalc.rhumbAzimuthBetween(selfLatLon, pointLatLon), usesDeclination);
                            pointItem.put(ATTRIBUTE_AZIMUTH, Math.round(azimuth));

                            double distance = GeoCalc.toRealDistance(GeoCalc.rhumbDistanceBetween(selfLatLon, pointLatLon));
                            pointItem.put(ATTRIBUTE_AZIMUTH, Math.round(azimuth));
                            pointItem.put(ATTRIBUTE_DISTANCE, Math.round(distance));
                            data.set(i, pointItem);
                        } catch (NumberFormatException e) {
                            //
                        }
                    }
                    pointsAdapter.notifyDataSetChanged();
                }
            }
        };
        pointService = new PointService();
        loadPoints();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.points_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.points_menu_add_point:
                addPointDialog();
                break;
            case R.id.points_menu_projection:
                projectionDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void projectionDialog(){
        ProjectionDialog projectionDialog = new ProjectionDialog();
        Bundle bundle = new Bundle();
        if (lastLocation != null) {
            Double lat = new BigDecimal(lastLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            Double lon = new BigDecimal(lastLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            bundle.putString(LATITUDE, String.valueOf(lat));
            bundle.putString(LONGITUDE, String.valueOf(lon));
        }

        bundle.putString("name", new Date().toString());
        projectionDialog.setArguments(bundle);


        projectionDialog.setCancelable(false);
        projectionDialog.show(getSupportFragmentManager(), PROJECTION_DIALOG_TAG);
    }

    private void addPointDialog(){
        PointEditDialog pointDialog = new PointEditDialog();
        Bundle bundle = new Bundle();
        if (lastLocation != null) {
            Double lat = new BigDecimal(lastLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            Double lon = new BigDecimal(lastLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            bundle.putString(LATITUDE, String.valueOf(lat));
            bundle.putString(LONGITUDE, String.valueOf(lon));
        }

        bundle.putString("name", new Date().toString());
        pointDialog.setArguments(bundle);


        pointDialog.setCancelable(false);
        pointDialog.show(getSupportFragmentManager(), POINT_DIALOG_TAG);
    }

    private void loadPoints() {
        List<Point> points = pointService.getPoints();

        data = new ArrayList<>();
        pointIdListIndexMap = new HashMap<>();
        Map<String, Object> m;
        for (Point p : points) {
            m = new HashMap<>();
            m.put(PointService.ATTRIBUTE_NAME_ID, p.id);
            m.put(PointService.ATTRIBUTE_NAME_NAME, p.name);
            m.put(PointService.ATTRIBUTE_NAME_LAT, p.lat);
            m.put(PointService.ATTRIBUTE_NAME_LON, p.lon);
            m.put(ATTRIBUTE_AZIMUTH, "N/A");
            m.put(ATTRIBUTE_DISTANCE, "N/A");
            data.add(m);
            int index = data.indexOf(m);
            pointIdListIndexMap.put(p.id, index);
        }

        String[] from = {
                PointService.ATTRIBUTE_NAME_NAME,
                PointService.ATTRIBUTE_NAME_LAT,
                PointService.ATTRIBUTE_NAME_LON,
                ATTRIBUTE_AZIMUTH,
                ATTRIBUTE_DISTANCE
        };

        int[] to = {
                R.id.tvPointName,
                R.id.tvPointLat,
                R.id.tvPointLon,
                R.id.tvPointAzimuth,
                R.id.tvPointDistance
        };
        pointsAdapter = new PointsAdapter(this, data, R.layout.point_item, from, to);

        lvPoints.setAdapter(pointsAdapter);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_EDIT_ID, 0, R.string.edit);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == CM_DELETE_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Map<String, Object> pointData = data.get(acmi.position);
            long pointId = (Long) pointData.get(PointService.ATTRIBUTE_NAME_ID);
            pointService.deletePoint(pointId);
            data.remove(acmi.position);
            pointIdListIndexMap.remove(pointId);
            pointsAdapter.notifyDataSetChanged();
            return true;

        } else if (item.getItemId() == CM_EDIT_ID) {

            PointEditDialog pointDialog = new PointEditDialog();
            Bundle bundle = new Bundle();

            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Map<String, Object> pointData = data.get(acmi.position);

            long pointId = (Long) pointData.get(PointService.ATTRIBUTE_NAME_ID);
            Point point = pointService.getPoint(pointId);

            bundle.putLong("pointId", point.id);
            bundle.putString("latitude", point.lat);
            bundle.putString("longitude", point.lon);
            bundle.putString("name", point.name);
            pointDialog.setArguments(bundle);

            pointDialog.setCancelable(false);
            pointDialog.show(getSupportFragmentManager(), POINT_DIALOG_TAG);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        registerReceiver(br, new IntentFilter(GpsDataService.LOCATION_BROADCAST_ACTION));
        usingDeclination = sp.getBoolean("declination", false);
        pointsAdapter.setMagnetDeclinationUsing(usingDeclination);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(br);
        super.onPause();
    }

    @Override
    public void pointResult(Point point) {
        HashMap<String, Object> pointData = new HashMap<>();
        pointData.put(PointService.ATTRIBUTE_NAME_ID, point.id);
        pointData.put(PointService.ATTRIBUTE_NAME_NAME, point.name);
        pointData.put(PointService.ATTRIBUTE_NAME_LAT, point.lat);
        pointData.put(PointService.ATTRIBUTE_NAME_LON, point.lon);
        pointData.put(ATTRIBUTE_AZIMUTH, "N/A");
        pointData.put(ATTRIBUTE_DISTANCE, "N/A");
        if (pointIdListIndexMap.containsKey(point.id)) {
            int index = pointIdListIndexMap.get(point.id);
            data.set(index, pointData);
        } else {
            data.add(pointData);
        }
        pointsAdapter.notifyDataSetChanged();
    }
}
