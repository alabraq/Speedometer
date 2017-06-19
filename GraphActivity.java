package com.example.r61.speedometer;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphActivity extends AppCompatActivity {

    static boolean isLocationChanged;
    static boolean autoScrollable = true;
    int borderX = 180;

    SharedPreferences preferences;
    Handler handler = new Handler();
    Runnable timer;
    LineGraphSeries<DataPoint> speedGraphSeries;
    LineGraphSeries<DataPoint> averageSpeedGraphSeries;

    RelativeLayout activityGraph;
    GraphView graphView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        isLocationChanged = getIntent().getBooleanExtra("isLocationChanged", false);

        initialization();
        setScreenAlwaysOn();
        setAutoScrollable();

        preferences = getSharedPreferences("pref", MODE_PRIVATE);
        changeBackground(preferences.getInt("screenState", 1));
        changeUnits(preferences.getInt("unitsState", 1));

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(borderX);

    }

    @Override
    protected void onResume() {
        super.onResume();

        speedGraphSeries = new LineGraphSeries<>();
        speedGraphSeries.setColor(getResources().getColor(R.color.speedGraphSeries));
        averageSpeedGraphSeries = new LineGraphSeries<>();
        averageSpeedGraphSeries.setColor(getResources().getColor(R.color.averageSpeedGraphSeries));

        graphView.addSeries(averageSpeedGraphSeries);
        graphView.addSeries(speedGraphSeries);

        if (isLocationChanged) {
            if (MainActivity.time < borderX) {
                for (DataPoint data : MainActivity.dataPoints) {
                    speedGraphSeries.appendData(convertDataPoint(preferences.getInt("unitsState", 1), data), false, MainActivity.time);
                }
                graphView.getViewport().setMinX(0);
                graphView.getViewport().setMaxX(borderX);
            } else {
                for (DataPoint data : MainActivity.dataPoints) {
                    speedGraphSeries.appendData(convertDataPoint(preferences.getInt("unitsState", 1), data), autoScrollable, MainActivity.time);
                }
                graphView.getViewport().setMinX(MainActivity.time - borderX);
                graphView.getViewport().setMaxX(MainActivity.time);
            }

            timer = new Runnable() {
                @Override
                public void run() {
                    if (MainActivity.time < borderX) {

                        speedGraphSeries.appendData(convertDataPoint(preferences.getInt("unitsState", 1), MainActivity.dataPoints.get(MainActivity.dataPoints.size() - 1)), false, MainActivity.time);

                        lineGraphAverageSpeed(preferences.getInt("unitsState", 1));
                        infoChangeAverageSpeed();

                    } else {

                        speedGraphSeries.appendData(convertDataPoint(preferences.getInt("unitsState", 1), MainActivity.dataPoints.get(MainActivity.dataPoints.size() - 1)), autoScrollable, MainActivity.time);

                        lineGraphAverageSpeed(preferences.getInt("unitsState", 1));
                        infoChangeAverageSpeed();

                    }
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(timer, 0);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timer);
    }


    private void initialization() {
        activityGraph = (RelativeLayout) findViewById(R.id.activity_graph);
        graphView = (GraphView) findViewById(R.id.graphView);
    }

    private void setScreenAlwaysOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setAutoScrollable() {
        graphView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (MainActivity.time >= borderX) {
                    if (autoScrollable) {
                        autoScrollable = false;
                        Toast.makeText(GraphActivity.this, "Auto Scrollable OFF", Toast.LENGTH_SHORT).show();
                    } else {
                        autoScrollable = true;
                        Toast.makeText(GraphActivity.this, "Auto Scrollable ON", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
    }

    private void changeBackground(int screenState) {
        if (screenState == 1) {
            activityGraph.setBackgroundColor(getResources().getColor(R.color.white));
            preferences.edit().putInt("screenState", 1).apply();
        }
        if (screenState == -1) {
            graphView.setBackgroundColor(getResources().getColor(R.color.grey));
            preferences.edit().putInt("screenState", -1).apply();
        }
    }

    private void changeUnits(int unitsState) {
        if (unitsState == 1) {
            setTitle("Speed chart   KM/H");
        }
        if (unitsState == -1) {
            setTitle("Speed chart   M/S");
        }
    }

    private DataPoint convertDataPoint(int unitsState, DataPoint data) {
        if (unitsState == 1) {
            return new DataPoint(data.getX(), data.getY() * 3.6);
        }
        else {
            return data;
        }
    }

    private void lineGraphAverageSpeed(int unitsState) {
        DataPoint[] line = new DataPoint[2];
        if (unitsState == 1) {
            line[0] = new DataPoint(0, MainActivity.averageSpeed * 3.6);
            if (MainActivity.time < borderX) {
                line[1] = new DataPoint(borderX, MainActivity.averageSpeed * 3.6);
            } else {
                line[1] = new DataPoint(MainActivity.time, MainActivity.averageSpeed * 3.6);
            }
        }
        if (unitsState == -1) {
            line[0] = new DataPoint(0, MainActivity.averageSpeed);
            if (MainActivity.time < borderX) {
                line[1] = new DataPoint(borderX, MainActivity.averageSpeed);
            } else {
                line[1] = new DataPoint(MainActivity.time, MainActivity.averageSpeed);
            }
        }
        averageSpeedGraphSeries.resetData(line);
    }

    private void infoChangeAverageSpeed() {
        if (MainActivity.averageSpeed > MainActivity.speed) {
            averageSpeedGraphSeries.setColor(getResources().getColor(R.color.averageSpeedGraphSeriesLow));
        }
        if (MainActivity.averageSpeed == MainActivity.speed) {
            averageSpeedGraphSeries.setColor(getResources().getColor(R.color.averageSpeedGraphSeries));
        }
        if (MainActivity.averageSpeed < MainActivity.speed) {
            averageSpeedGraphSeries.setColor(getResources().getColor(R.color.averageSpeedGraphSeriesHigh));
        }
    }


}
