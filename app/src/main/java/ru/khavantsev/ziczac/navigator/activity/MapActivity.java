package ru.khavantsev.ziczac.navigator.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.geo.GeoCalc;
import ru.khavantsev.ziczac.navigator.geo.LatLon;
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private double scale = 2; // meters per pixel
    private Location location = null;
    private boolean refreshLocation = true;
    private List<Point> points = null;
    private BroadcastReceiver br;
    private PointService pointService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, GpsDataService.class));
        this.setTitle(R.string.title_activity_map);
        setContentView(new DrawView(this));

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                location = intent.getParcelableExtra(GpsDataService.LOCATION_BROADCAST_EXTRA_LOCATION);
                refreshLocation = true;
            }
        };

        pointService = new PointService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(GpsDataService.LOCATION_BROADCAST_ACTION));
        points = pointService.getPoints();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
        location = null;
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, GpsDataService.class));
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location_settings:
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;
            case R.id.menu_points:
                startActivity(new Intent(this, PointsActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            scale *= 1.5;
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (scale > 1) {
                scale /= 1.5;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    private class DrawView extends SurfaceView implements SurfaceHolder.Callback {

        private DrawThread drawThread;

        public DrawView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        class DrawThread extends Thread {

            Paint paint;
            int canvasWidth;
            int canvasHeight;
            float bearing = 0;
            float canvasCenterPointLeft;
            float canvasCenterPointTop;
            List<CanvasPoint> canvasPoints = new ArrayList<>();
            Bitmap cursor1lBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor1l);
            Bitmap cursorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
            private boolean running = false;
            private SurfaceHolder surfaceHolder;

            DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
                paint = new Paint();
            }

            void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                Canvas canvas;
                while (running) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        if (canvas == null) {
                            continue;
                        }
                        process(canvas);
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }

            private void process(Canvas canvas) {
                canvas.drawColor(getResources().getColor(R.color.colorWhite));
                canvasWidth = canvas.getWidth();
                canvasHeight = canvas.getHeight();

                canvasCenterPointLeft = canvasWidth / 2;
                canvasCenterPointTop = canvasHeight / 2;

                if (refreshLocation) {
                    refreshLocations();
                }

                for (CanvasPoint point : canvasPoints) {
                    paint.setAntiAlias(true);
                    if(point.drawLine){
                        paint.setStrokeWidth(2);
                        paint.setColor(getResources().getColor(R.color.colorBlue));
                        canvas.drawLine(canvasCenterPointLeft, canvasCenterPointTop, point.x, point.y, paint);
                    }

                    paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
                    canvas.drawCircle(point.x, point.y, 7, paint);

                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(18);
                    canvas.drawText(point.name, point.x, point.y - 10, paint);
                }

                canvas.drawBitmap(cursor1lBitmap, canvasCenterPointLeft - cursor1lBitmap.getWidth() / 2, canvasCenterPointTop - cursor1lBitmap.getHeight() / 2, paint);
                Bitmap cursor = rotateBitmap(cursorBitmap, bearing);
                canvas.drawBitmap(cursor, canvasCenterPointLeft - cursor.getWidth() / 2, canvasCenterPointTop - cursor.getHeight() / 2, paint);
            }

            void refreshLocations() {
                LatLon currentPoint = new LatLon();
                if (location != null) {
                    currentPoint.latitude = location.getLatitude();
                    currentPoint.longitude = location.getLongitude();
                    bearing = location.getBearing();
                }

                canvasPoints.clear();
                for (Point point : points) {
                    if (point.enable == 1) {
                        LatLon pointLatLon = new LatLon(Double.parseDouble(point.lat), Double.parseDouble(point.lon));
                        double angle = GeoCalc.rhumbAzimuthBetween(currentPoint, pointLatLon);
                        double distance = GeoCalc.toRealDistance(GeoCalc.rhumbDistanceBetween(currentPoint, pointLatLon));
                        float pixelDistance = Math.round(distance / scale);
                        double pointX = canvasCenterPointLeft + pixelDistance * Math.sin(angle);
                        double pointY = canvasCenterPointTop - pixelDistance * Math.cos(angle);

                        canvasPoints.add(new CanvasPoint((float) pointX, (float) pointY, point.name, point.drawLine == 1));
                    }
                }
                refreshLocation = false;
            }

            Bitmap rotateBitmap(Bitmap source, float angle) {
                Matrix matrix = new Matrix();
                matrix.postRotate(angle);
                return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            }

            class CanvasPoint {
                float x;
                float y;
                String name;
                Boolean drawLine;

                CanvasPoint(float x, float y, String name, Boolean drawLine) {
                    this.x = x;
                    this.y = y;
                    this.name = name;
                    this.drawLine = drawLine;
                }
            }

        }

    }

}
