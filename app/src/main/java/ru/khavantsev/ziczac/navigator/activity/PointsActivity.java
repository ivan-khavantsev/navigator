package ru.khavantsev.ziczac.navigator.activity;

import android.app.DialogFragment;
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
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsActivity extends AppCompatActivity {

    public static final String LOG_TAG = PointsActivity.class.toString();
    public static final int CM_DELETE_ID = 1;

    public static final String ATTRIBUTE_AZIMUTH = "azimuth";

    ListView lvPoints;
    SimpleAdapter sAdapter;
    ArrayList<Map<String, Object>> data;

    private BroadcastReceiver br;

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
                DialogFragment dlg1 = new PointAddDialog();
                dlg1.setCancelable(false);
                dlg1.show(getFragmentManager(), "dlg1");
            }
        });

        lvPoints = (ListView) findViewById(R.id.lvPoints);
        registerForContextMenu(lvPoints);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Location location = intent.getParcelableExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_NAME);
                if (data != null) {
                    for(int i = 0; i<data.size();i++){
                        Map<String, Object> pointItem = data.get(i);

                        //Get LAT,LON..etc and update fields

                        pointItem.put(ATTRIBUTE_AZIMUTH, location.getTime());
                        data.set(i, pointItem);
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
            data.add(m);
        }
        String[] from = {
                PointService.ATTRIBUTE_NAME_ID,
                PointService.ATTRIBUTE_NAME_NAME,
                PointService.ATTRIBUTE_NAME_LAT,
                PointService.ATTRIBUTE_NAME_LON,
                ATTRIBUTE_AZIMUTH
        };

        int[] to = {
                R.id.stubPointId,
                R.id.tvPointName,
                R.id.tvPointLat,
                R.id.tvPointLon,
                R.id.tvPointAzimuth
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
            int id = (Integer) pointData.get(PointService.ATTRIBUTE_NAME_ID);
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

}
