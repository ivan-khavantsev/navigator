package ru.khavantsev.ziczac.navigator.activity;

import android.app.DialogFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.db.DBHelper;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.dialog.PointAddDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsActivity extends AppCompatActivity {

    public static final String LOG_TAG = PointsActivity.class.toString();

    ListView lvPoints;
    SimpleAdapter sAdapter;
    ArrayList<Map<String, Object>> data;
    Map<String, Object> m;

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
        loadPoints();
    }

    final String ATTRIBUTE_NAME_NAME = "name";
    final String ATTRIBUTE_NAME_LAT = "lat";
    final String ATTRIBUTE_NAME_LON = "lon";

    private void loadPoints() {

        PointService ps = new PointService();
        List<Point> points = ps.getPoints();

        data = new ArrayList<>();
        for (Point p : points) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_NAME, p.name);
            m.put(ATTRIBUTE_NAME_LAT, p.lat);
            m.put(ATTRIBUTE_NAME_LON, p.lon);
            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_NAME, ATTRIBUTE_NAME_LAT, ATTRIBUTE_NAME_LON};
        int[] to = {R.id.tvPointName, R.id.tvPointLat, R.id.tvPointLon};
        sAdapter = new SimpleAdapter(this, data, R.layout.point_item, from, to);

        lvPoints = (ListView) findViewById(R.id.lvPoints);
        lvPoints.setAdapter(sAdapter);
    }
}
