package ru.khavantsev.ziczac.navigator.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.db.DBHelper;

public class PointsActivity extends AppCompatActivity {

    public static final String LOG_TAG = PointsActivity.class.toString();

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_point);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        dbHelper = new DBHelper(this);
    }


    @Override
    protected void onResume() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.query("points", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int latColIndex = c.getColumnIndex("lat");
            int lonColIndex = c.getColumnIndex("lon");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                                ", name = " + c.getString(nameColIndex) +
                                ", lat = " + c.getString(latColIndex) +
                                ", lon = " + c.getString(lonColIndex));
            } while (c.moveToNext());

            super.onResume();
        } else {
            Log.d(LOG_TAG, "Empty database");
        }
        c.close();
        super.onResume();
    }

}
