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
import ru.khavantsev.ziczac.navigator.util.ZzMath;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class PointsActivity extends AppCompatActivity implements PointListener {

    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;
    private static final int CM_PROJECTION_ID = 3;

    private static final String ATTRIBUTE_AZIMUTH = "azimuth";
    private static final String ATTRIBUTE_DISTANCE = "distance";
    private static final String ATTRIBUTE_COUNTER = "counter";


    private static final String NAME_DATE_FORMAT = "yyyy-MM-dd-E-HH:mm";
    private static final String POINT_ID = "pointId";
    private static final String NAME = "name";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String DECLINATION = "declination";

    private static final String POINT_DIALOG_TAG = "Point";
    private static final String PROJECTION_DIALOG_TAG = "Projection";

    private static final String COUNTER_KM = "km";
    private static final String COUNTER_M = "m";

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
                            azimuth = new BigDecimal(azimuth).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            if (azimuth == 360.0) {
                                azimuth = 0.0;
                            }
                            pointItem.put(ATTRIBUTE_AZIMUTH, azimuth);

                            double distance = (GeoCalc.toRealDistance(GeoCalc.rhumbDistanceBetween(selfLatLon, pointLatLon)));
                            String distanceFormatted;
                            if (distance >= 1000) { // Один километр
                                distance /= 1000;
                                distanceFormatted = String.valueOf(ZzMath.round(distance, 1));
                                pointItem.put(ATTRIBUTE_COUNTER, COUNTER_KM);

                            } else {
                                distanceFormatted = String.valueOf(Math.round(distance));
                                pointItem.put(ATTRIBUTE_COUNTER, COUNTER_M);
                            }

                            pointItem.put(ATTRIBUTE_DISTANCE, distanceFormatted);

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

    private void projectionDialog() {
        ProjectionDialog projectionDialog = new ProjectionDialog();
        Bundle bundle = new Bundle();
        if (lastLocation != null) {
            Double lat = new BigDecimal(lastLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            Double lon = new BigDecimal(lastLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            bundle.putString(LATITUDE, String.valueOf(lat));
            bundle.putString(LONGITUDE, String.valueOf(lon));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(NAME_DATE_FORMAT);
        bundle.putString(NAME, dateFormat.format(new Date()));

        bundle.putFloat(DECLINATION, lastDeclination);

        projectionDialog.setArguments(bundle);

        projectionDialog.setCancelable(false);
        projectionDialog.show(getSupportFragmentManager(), PROJECTION_DIALOG_TAG);
    }

    private void addPointDialog() {
        PointEditDialog pointDialog = new PointEditDialog();
        Bundle bundle = new Bundle();
        if (lastLocation != null) {
            Double lat = new BigDecimal(lastLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            Double lon = new BigDecimal(lastLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            bundle.putString(LATITUDE, String.valueOf(lat));
            bundle.putString(LONGITUDE, String.valueOf(lon));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(NAME_DATE_FORMAT);
        bundle.putString(NAME, dateFormat.format(new Date()));
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
            m.put(PointService.ATTRIBUTE_NAME_DRAW_LINE, p.drawLine==1);
            m.put(PointService.ATTRIBUTE_NAME_ENABLE, p.enable==1);
            m.put(ATTRIBUTE_AZIMUTH, "N/A");
            m.put(ATTRIBUTE_DISTANCE, "N/A");
            data.add(m);
            int index = data.indexOf(m);
            pointIdListIndexMap.put(p.id, index);
        }

        String[] from = {
                PointService.ATTRIBUTE_NAME_ID,
                PointService.ATTRIBUTE_NAME_NAME,
                PointService.ATTRIBUTE_NAME_LAT,
                PointService.ATTRIBUTE_NAME_LON,
                PointService.ATTRIBUTE_NAME_DRAW_LINE,
                PointService.ATTRIBUTE_NAME_ENABLE,
                ATTRIBUTE_AZIMUTH,
                ATTRIBUTE_DISTANCE,
                ATTRIBUTE_COUNTER
        };

        int[] to = {
                R.id.tvPointId,
                R.id.tvPointName,
                R.id.tvPointLat,
                R.id.tvPointLon,
                R.id.tvPointCheckboxLine,
                R.id.tvPointCheckboxEnable,
                R.id.tvPointAzimuth,
                R.id.tvPointDistance,
                R.id.tvPointCounter
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
        menu.add(0, CM_PROJECTION_ID, 0, R.string.projection);
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

            bundle.putLong(POINT_ID, point.id);
            pointDialog.setArguments(bundle);

            pointDialog.setCancelable(false);
            pointDialog.show(getSupportFragmentManager(), POINT_DIALOG_TAG);

        } else if (item.getItemId() == CM_PROJECTION_ID) {

            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Map<String, Object> pointData = data.get(acmi.position);

            long pointId = (Long) pointData.get(PointService.ATTRIBUTE_NAME_ID);
            Point point = pointService.getPoint(pointId);

            ProjectionDialog projectionDialog = new ProjectionDialog();
            Bundle bundle = new Bundle();
            bundle.putString(LATITUDE, point.lat);
            bundle.putString(LONGITUDE, point.lon);
            bundle.putFloat(DECLINATION, lastDeclination);

            bundle.putString(NAME, point.name + "-proj");
            projectionDialog.setArguments(bundle);

            projectionDialog.setCancelable(false);
            projectionDialog.show(getSupportFragmentManager(), PROJECTION_DIALOG_TAG);
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
        pointData.put(PointService.ATTRIBUTE_NAME_DRAW_LINE, point.drawLine==1);
        pointData.put(PointService.ATTRIBUTE_NAME_ENABLE, point.enable==1);
        pointData.put(ATTRIBUTE_AZIMUTH, "N/A");
        pointData.put(ATTRIBUTE_DISTANCE, "N/A");
        if (pointIdListIndexMap.containsKey(point.id)) {
            int index = pointIdListIndexMap.get(point.id);
            data.set(index, pointData);
        } else {
            data.add(pointData);
            int index = data.indexOf(pointData);
            pointIdListIndexMap.put(point.id, index);
        }
        pointsAdapter.notifyDataSetChanged();
    }
}
