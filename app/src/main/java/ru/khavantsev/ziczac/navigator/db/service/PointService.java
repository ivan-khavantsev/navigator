package ru.khavantsev.ziczac.navigator.db.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.khavantsev.ziczac.navigator.db.model.Point;

import java.util.ArrayList;
import java.util.List;


public class PointService extends Service {

    public static final String LOG_TAG = PointService.class.toString();
    public static final String POINTS_TABLE_NAME = "points";


    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

        Cursor c = db.query("points", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int latColIndex = c.getColumnIndex("lat");
            int lonColIndex = c.getColumnIndex("lon");

            do {
                Point point = new Point();
                point.id = c.getInt(idColIndex);
                point.name = c.getString(nameColIndex);
                point.lat = c.getString(latColIndex);
                point.lon = c.getString(lonColIndex);

                points.add(point);
            } while (c.moveToNext());

        }
        c.close();
        db.close();

        return points;
    }

    public void savePoint(Point point){
        ContentValues cv = new ContentValues();
        cv.put("name", point.name);
        cv.put("lat", point.lat);
        cv.put("lon", point.lon);

        SQLiteDatabase db = getDBHelper().getWritableDatabase();
        long rowID = db.insert(POINTS_TABLE_NAME, null, cv);
    }
}
