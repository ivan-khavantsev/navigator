package ru.khavantsev.ziczac.navigator.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.service.GpsDataService;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, GpsDataService.class));
        //setContentView(R.layout.activity_map);
        setContentView(new DrawView(this));
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

            Paint p;
            RectF rectf;
            float[] points;
            float[] points1;
            float sinlont = 0;
            private boolean running = false;
            private SurfaceHolder surfaceHolder;

            DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;

                p = new Paint();
                rectf = new RectF(700, 100, 800, 150);
                points = new float[]{100, 50, 150, 100, 150, 200, 50, 200, 50, 100};
                points1 = new float[]{300, 200, 600, 200, 300, 300, 600, 300, 400, 100, 400, 400, 500, 100, 500, 400};
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



                p.setColor(Color.RED);
                p.setStrokeWidth(10);

                // рисуем точки их массива points
                canvas.drawPoints(points,p);

                // рисуем линии по точкам из массива points1
                canvas.drawLines(points1,p);

                // перенастраиваем кисть на зеленый цвет
                p.setColor(Color.GREEN);

                // рисуем закругленный прямоугольник по координатам из rectf
                // радиусы закругления = 20
                canvas.drawRoundRect(rectf, 20, 20, p);

                // смещаем коорднаты rectf на 150 вниз
                rectf.offset(0, 150);
                // рисуем овал внутри прямоугольника rectf
                canvas.drawOval(rectf, p);

                // смещаем rectf в (900,100) (левая верхняя точка)
                rectf.offsetTo(900, 100);
                // увеличиваем rectf по вертикали на 25 вниз и вверх
                rectf.inset(0, -25);
                // рисуем дугу внутри прямоугольника rectf
                // с началом в 90, и длиной 270
                // соединение крайних точек через центр
                canvas.drawArc(rectf, 90, 270, true, p);

                // смещаем коорднаты rectf на 150 вниз
                rectf.offset(0, 150);
                // рисуем дугу внутри прямоугольника rectf
                // с началом в 90, и длиной 270
                // соединение крайних точек напрямую
                canvas.drawArc(rectf, 90, 270, false, p);

                // перенастраиваем кисть на толщину линии = 3
                p.setStrokeWidth(3);
                // рисуем линию (150,450) - (150,600)
                canvas.drawLine(150, 450, 150, 600, p);

                // перенастраиваем кисть на синий цвет
                p.setColor(Color.BLUE);

                // настраиваем размер текста = 30
                p.setTextSize(30);
                // рисуем текст в точке (150,500)
                canvas.drawText("text left", 150, 500, p);

                // настраиваем выравнивание текста на центр
                p.setTextAlign(Paint.Align.CENTER);
                // рисуем текст в точке (150,525)
                canvas.drawText("text center", 150, 525, p);

                // настраиваем выравнивание текста на левое
                p.setTextAlign(Paint.Align.RIGHT);
                // рисуем текст в точке (150,550)
                canvas.drawText("text right", 150, 550, p);


                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();

                p = new Paint();
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
                int bw = b.getWidth();
                int bh = b.getHeight();
                p.setColor(getResources().getColor(R.color.colorWhite));
                canvas.drawBitmap(b, canvasWidth / 2 - bw / 2, canvasHeight / 2 - bh / 2, p);

            }

        }

    }

}
