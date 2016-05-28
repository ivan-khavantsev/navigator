package ru.khavantsev.ziczac.navigator.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.dialog.PointAddDialog;
import ru.khavantsev.ziczac.navigator.geo.GeoCalc;
import ru.khavantsev.ziczac.navigator.geo.LatLon;
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

import java.math.BigDecimal;
import java.util.*;

public class PointsActivity extends AppCompatActivity implements PointListener {

    public static final String LOG_TAG = PointsActivity.class.toString();
    public static final int CM_DELETE_ID = 1;

    public static final String ATTRIBUTE_AZIMUTH = "azimuth";
    public static final String ATTRIBUTE_DISTANCE = "distance";

    public static final String POINT_DIALOG_TAG = "Point";

    ListView lvPoints;
    SimpleAdapter sAdapter;
    ArrayList<Map<String, Object>> data;

    private BroadcastReceiver br;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addPintButton = (FloatingActionButton) findViewById(R.id.add_point);
        addPintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PointAddDialog pointDialog = new PointAddDialog();
                Bundle bundle = new Bundle();
                if (lastLocation != null) {
                    Double lat = new BigDecimal(lastLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
                    Double lon = new BigDecimal(lastLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
                    bundle.putString("latitude", String.valueOf(lat));
                    bundle.putString("longitude", String.valueOf(lon));
                }

                bundle.putString("name", new Date().toString());
                pointDialog.setArguments(bundle);


                pointDialog.setCancelable(false);
                pointDialog.show(getFragmentManager(), POINT_DIALOG_TAG);
            }
        });

        lvPoints = (ListView) findViewById(R.id.lvPoints);
        registerForContextMenu(lvPoints);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                lastLocation = intent.getParcelableExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_NAME);
                LatLon selfLatLon = new LatLon(lastLocation.getLatitude(), lastLocation.getLongitude());
                if (data != null) {
                    for (int i = 0; i < data.size(); i++) {
                        try {
                            Map<String, Object> pointItem = data.get(i);

                            LatLon pointLatLon = new LatLon();
                            pointLatLon.latitude = Double.parseDouble((String) pointItem.get(PointService.ATTRIBUTE_NAME_LAT));
                            pointLatLon.longitude = Double.parseDouble((String) pointItem.get(PointService.ATTRIBUTE_NAME_LON));

                            double azimuth = GeoCalc.toRealAzimuth(GeoCalc.rhumbAzimuth(selfLatLon, pointLatLon));
                            pointItem.put(ATTRIBUTE_AZIMUTH, Math.round(azimuth));

                            double distance = GeoCalc.toRealDistance(GeoCalc.rhumbDistance(selfLatLon, pointLatLon));
                            pointItem.put(ATTRIBUTE_AZIMUTH, Math.round(azimuth));
                            pointItem.put(ATTRIBUTE_DISTANCE, Math.round(distance));
                            data.set(i, pointItem);
                        } catch (NumberFormatException e) {
                            //
                        }
                    }
                    sAdapter.notifyDataSetChanged();
                }
            }
        };

        loadPoints();
    }

    private void loadPoints() {

        PointService ps = new PointService();
        List<Point> points = ps.getPoints();

        data = new ArrayList<>();
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
        sAdapter = new SimpleAdapter(this, data, R.layout.point_item, from, to);


        lvPoints.setAdapter(sAdapter);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, "Удалить запись");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем инфу о пункте списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // удаляем Map из коллекции, используя позицию пункта в списке
            Map<String, Object> pointData = data.get(acmi.position);
            long id = (Long) pointData.get(PointService.ATTRIBUTE_NAME_ID);
            PointService ps = new PointService();
            ps.deletePoint(id);
            data.remove(acmi.position);
            // уведомляем, что данные изменились
            sAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
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

    @Override
    public void pointResult(Point point) {
        HashMap<String, Object> m = new HashMap<>();
        m.put(PointService.ATTRIBUTE_NAME_ID, point.id);
        m.put(PointService.ATTRIBUTE_NAME_NAME, point.name);
        m.put(PointService.ATTRIBUTE_NAME_LAT, point.lat);
        m.put(PointService.ATTRIBUTE_NAME_LON, point.lon);
        m.put(ATTRIBUTE_AZIMUTH, "N/A");
        m.put(ATTRIBUTE_DISTANCE, "N/A");
        data.add(m);
        sAdapter.notifyDataSetChanged();
    }
}
