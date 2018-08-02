package ru.khavantsev.ziczac.navigator.db.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.khavantsev.ziczac.navigator.db.model.Point;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;


public class PointService extends Service {

    public static final String ATTRIBUTE_NAME_ID = "id";
    public static final String ATTRIBUTE_NAME_NAME = "name";
    public static final String ATTRIBUTE_NAME_LAT = "lat";
    public static final String ATTRIBUTE_NAME_LON = "lon";
    public static final String ATTRIBUTE_NAME_DRAW_LINE = "drawLine";
    private static final String POINTS_TABLE_NAME = "points";

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

        Cursor c = db.query("points", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex(ATTRIBUTE_NAME_ID);
            int nameColIndex = c.getColumnIndex(ATTRIBUTE_NAME_NAME);
            int latColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LAT);
            int lonColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LON);
            int drawLineColIndex = c.getColumnIndex(ATTRIBUTE_NAME_DRAW_LINE);

            do {
                Point point = new Point();
                point.id = c.getInt(idColIndex);
                point.name = c.getString(nameColIndex);
                point.lat = c.getString(latColIndex);
                point.lon = c.getString(lonColIndex);
                point.drawLine = c.getInt(drawLineColIndex);

                points.add(point);
            } while (c.moveToNext());

        }
        c.close();
        db.close();

        return points;
    }

    public long savePoint(Point point) {
        ContentValues cv = new ContentValues();

        cv.put(ATTRIBUTE_NAME_NAME, point.name);
        cv.put(ATTRIBUTE_NAME_LAT, point.lat);
        cv.put(ATTRIBUTE_NAME_LON, point.lon);
        cv.put(ATTRIBUTE_NAME_DRAW_LINE, point.drawLine);

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

        long pointId;
        if (point.id == 0) {
            point.id = db.insertWithOnConflict(POINTS_TABLE_NAME, null, cv, CONFLICT_REPLACE);
        } else {
            db.update(POINTS_TABLE_NAME, cv, "id = ?", new String[]{String.valueOf(point.id)});
        }
        return point.id;
    }

    public void deletePoint(long id) {
        SQLiteDatabase db = getDBHelper().getWritableDatabase();
        db.delete(POINTS_TABLE_NAME, ATTRIBUTE_NAME_ID + " = " + id, null);
    }

    public Point getPoint(long pointId) {
        Point point = null;
        SQLiteDatabase db = getDBHelper().getWritableDatabase();
        Cursor c = db.query("points", null, "id = ?", new String[]{String.valueOf(pointId)}, null, null, null);
        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex(ATTRIBUTE_NAME_ID);
            int nameColIndex = c.getColumnIndex(ATTRIBUTE_NAME_NAME);
            int latColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LAT);
            int lonColIndex = c.getColumnIndex(ATTRIBUTE_NAME_LON);
            int drawLineColIndex = c.getColumnIndex(ATTRIBUTE_NAME_DRAW_LINE);

            point = new Point();
            point.id = c.getInt(idColIndex);
            point.name = c.getString(nameColIndex);
            point.lat = c.getString(latColIndex);
            point.lon = c.getString(lonColIndex);
            point.drawLine = c.getInt(drawLineColIndex);
        }
        c.close();
        db.close();

        return point;
    }
}
