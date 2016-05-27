package ru.khavantsev.ziczac.navigator.activity;

import android.app.DialogFragment;
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
import ru.khavantsev.ziczac.navigator.dialog.PointAddDialog;

public class PointsActivity extends AppCompatActivity {

    public static final String LOG_TAG = PointsActivity.class.toString();

    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);

        FloatingActionButton addPintButton = (FloatingActionButton) findViewById(R.id.add_point);
        addPintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dlg1 = new PointAddDialog();
                dlg1.setCancelable(false);
                dlg1.show(getFragmentManager(), "dlg1");
            }
        });


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
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                                ", name = " + c.getString(nameColIndex) +
                                ", lat = " + c.getString(latColIndex) +
                                ", lon = " + c.getString(lonColIndex));
            } while (c.moveToNext());

        } else {
            Log.d(LOG_TAG, "Empty database");
        }
        c.close();
        db.close();
        super.onResume();
    }

}
