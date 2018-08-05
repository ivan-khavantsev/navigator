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
    public static final String ATTRIBUTE_NAME_ENABLE = "enable";
    private static final String POINTS_TABLE_NAME = "points";

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

        Cursor cursor = db.query(POINTS_TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Point point = buildPoint(cursor);
                points.add(point);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        return points;
    }

    public long savePoint(Point point) {
        ContentValues cv = new ContentValues();

        cv.put(ATTRIBUTE_NAME_NAME, point.name);
        cv.put(ATTRIBUTE_NAME_LAT, point.lat);
        cv.put(ATTRIBUTE_NAME_LON, point.lon);
        cv.put(ATTRIBUTE_NAME_DRAW_LINE, point.drawLine);
        cv.put(ATTRIBUTE_NAME_ENABLE, point.enable);

        SQLiteDatabase db = getDBHelper().getWritableDatabase();

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
        Cursor cursor = db.query(POINTS_TABLE_NAME, null, "id = ?", new String[]{String.valueOf(pointId)}, null, null, null);
        if (cursor.moveToFirst()) {
            point = buildPoint(cursor);
        }
        cursor.close();
        db.close();

        return point;
    }

    private Point buildPoint(Cursor cursor) {
        int idColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_ID);
        int nameColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_NAME);
        int latColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_LAT);
        int lonColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_LON);
        int drawLineColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_DRAW_LINE);
        int enableColIndex = cursor.getColumnIndex(ATTRIBUTE_NAME_ENABLE);

        Point point = new Point();
        point.id = cursor.getInt(idColIndex);
        point.name = cursor.getString(nameColIndex);
        point.lat = cursor.getString(latColIndex);
        point.lon = cursor.getString(lonColIndex);
        point.drawLine = cursor.getInt(drawLineColIndex);
        point.enable = cursor.getInt(enableColIndex);
        return point;
    }

}
