package ru.khavantsev.ziczac.navigator.db.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.khavantsev.ziczac.navigator.db.model.Point;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;


public class PointService extends Service {

    public static final String LOG_TAG = PointService.class.toString();
    public static final String POINTS_TABLE_NAME = "points";

    public static final String ATTRIBUTE_NAME_ID = "id";
    public static final String ATTRIBUTE_NAME_NAME = "name";
    public static final String ATTRIBUTE_NAME_LAT = "lat";
    public static final String ATTRIBUTE_NAME_LON = "lon";

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

        Cursor c = db.query("points", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex(ATTRIBUTE_NAME_ID);
            int nameColIndex = c.getColumnIndex(ATTRIBUTE_NAME_NAME);
            int latColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LAT);
            int lonColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LON);

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

    public void savePoint(Point point) {
        ContentValues cv = new ContentValues();

        cv.put(ATTRIBUTE_NAME_ID, point.id);
        cv.put(ATTRIBUTE_NAME_NAME, point.name);
        cv.put(ATTRIBUTE_NAME_LAT, point.lat);
        cv.put(ATTRIBUTE_NAME_LON, point.lon);

        SQLiteDatabase db = getDBHelper().getWritableDatabase();
        long rowID = db.insertWithOnConflict(POINTS_TABLE_NAME, null, cv, CONFLICT_REPLACE);
    }

    public void deletePoint(int id) {
        SQLiteDatabase db = getDBHelper().getWritableDatabase();
        db.delete(POINTS_TABLE_NAME, ATTRIBUTE_NAME_ID + " = " + id, null);
    }
}
