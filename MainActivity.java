package com.example.r61.speedometer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static boolean isLocationChanged = false;
    static int time = 1;
    static float speed = 0, averageSpeed = 0, maxSpeed = 0, distance = 0;
    static double lastLatitude, lastLongitude;
    static ArrayList<DataPoint> dataPoints = new ArrayList<>();

    SharedPreferences preferences;
    Handler handler = new Handler();
    Runnable timer;

    RelativeLayout activityMain;
    TextView tvEmptyBox, tvUnits, tvSpeed, tvAverageSpeed, tvAverageSpeedLabel, tvMaxSpeed, tvMaxSpeedLabel, tvDistance, tvDistanceLabel;
    ImageButton ibtChangeBackground, ibtChangeUnits, ibtChart, ibtReset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();

        preferences = getSharedPreferences("pref", MODE_PRIVATE);
        changeBackground(preferences.getInt("screenState", 1));
        changeUnits(preferences.getInt("unitsState", 1));


        setIbtChangeBackgroundClick();
        setIbtChangeUnitsClick();
        setIbtChartClick();
        setIbtResetClick();


        setDataAfterRotation();
        setLayoutAfterRotation();
        setScreenAlwaysOn();
        changeFont();
        setGridLayout();


        GPSAlertDialog();
        GPS();

    }


    private void initialization() {
        activityMain = (RelativeLayout) findViewById(R.id.activity_main);
        tvEmptyBox = (TextView) findViewById(R.id.tvEmptyBox);
        tvUnits = (TextView) findViewById(R.id.tvUnits);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvAverageSpeed = (TextView) findViewById(R.id.tvAverageSpeed);
        tvAverageSpeedLabel = (TextView) findViewById(R.id.tvAverageSpeedLabel);
        tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
        tvMaxSpeedLabel = (TextView) findViewById(R.id.tvMaxSpeedLabel);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDistanceLabel = (TextView) findViewById(R.id.tvDistanceLabel);
        ibtChangeBackground = (ImageButton) findViewById(R.id.ibtChangeBackground);
        ibtChangeUnits = (ImageButton) findViewById(R.id.ibtChangeUnits);
        ibtChart = (ImageButton) findViewById(R.id.ibtChart);
        ibtReset = (ImageButton) findViewById(R.id.ibtReset);
    }


    private void setIbtChangeBackgroundClick() {
        ibtChangeBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeBackgroundPreferences(preferences.getInt("screenState", 1));
                changeBackground(preferences.getInt("screenState", 1));
            }
        });
    }

    private void changeBackgroundPreferences(int screenState) {
        if (screenState == 1) {
            preferences.edit().putInt("screenState", -1).apply();
        }
        if (screenState == -1) {
            preferences.edit().putInt("screenState", 1).apply();
        }
    }

    private void changeBackground(int screenState) {
        if (screenState == 1) {
            activityMain.setBackgroundColor(getResources().getColor(R.color.white));
            tvUnits.setTextColor(getResources().getColor(R.color.black));
            tvSpeed.setTextColor(getResources().getColor(R.color.black));
            tvMaxSpeed.setTextColor(getResources().getColor(R.color.black));
            tvMaxSpeedLabel.setTextColor(getResources().getColor(R.color.black));
            tvAverageSpeed.setTextColor(getResources().getColor(R.color.black));
            tvAverageSpeedLabel.setTextColor(getResources().getColor(R.color.black));
            tvDistance.setTextColor(getResources().getColor(R.color.black));
            tvDistanceLabel.setTextColor(getResources().getColor(R.color.black));
            ibtChangeBackground.setBackgroundResource(R.drawable.ic_exposure_black_36dp);
            ibtChangeUnits.setBackgroundResource(R.drawable.ic_change_history_black_36dp);
            ibtChart.setBackgroundResource(R.drawable.ic_timeline_black_36dp);
            ibtReset.setBackgroundResource(R.drawable.ic_restore_black_36dp);
        }
        if (screenState == -1) {
            activityMain.setBackgroundColor(getResources().getColor(R.color.black));
            tvUnits.setTextColor(getResources().getColor(R.color.white));
            tvSpeed.setTextColor(getResources().getColor(R.color.white));
            tvMaxSpeed.setTextColor(getResources().getColor(R.color.white));
            tvMaxSpeedLabel.setTextColor(getResources().getColor(R.color.white));
            tvAverageSpeed.setTextColor(getResources().getColor(R.color.white));
            tvAverageSpeedLabel.setTextColor(getResources().getColor(R.color.white));
            tvDistance.setTextColor(getResources().getColor(R.color.white));
            tvDistanceLabel.setTextColor(getResources().getColor(R.color.white));
            ibtChangeBackground.setBackgroundResource(R.drawable.ic_exposure_white_36dp);
            ibtChangeUnits.setBackgroundResource(R.drawable.ic_change_history_white_36dp);
            ibtChart.setBackgroundResource(R.drawable.ic_timeline_white_36dp);
            ibtReset.setBackgroundResource(R.drawable.ic_restore_white_36dp);
        }
    }


    private void setIbtChangeUnitsClick() {
        ibtChangeUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUnitsPreferences(preferences.getInt("unitsState", 1));
                changeUnits(preferences.getInt("unitsState", 1));
                convertData(preferences.getInt("unitsState", 1));
            }
        });
    }

    private void changeUnitsPreferences(int unitsState) {
        if (unitsState == 1) {
            preferences.edit().putInt("unitsState", -1).apply();
        }
        if (unitsState == -1) {
            preferences.edit().putInt("unitsState", 1).apply();
        }
    }

    private void changeUnits(int unitsState) {
        if (unitsState == 1) {
            tvUnits.setText("KM/H");
        }
        if (unitsState == -1) {
            tvUnits.setText("M/S");
        }
    }

    private void convertData(int unitsState) {
        DecimalFormat decimalFormat_1 = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat_1.setRoundingMode(RoundingMode.FLOOR);
        decimalFormat_1.setMaximumFractionDigits(1);

        if (unitsState == 1) {
            tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed * 3.6)));
            tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed * 3.6)));
            tvDistance.setText(String.valueOf(decimalFormat_1.format(distance / 1000)));
        }
        if (unitsState == -1) {
            tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed)));
            tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed)));
            tvDistance.setText(String.valueOf(decimalFormat_1.format(distance)));
        }

        if (isLocationChanged) {
            if (averageSpeed == 0) {
                tvAverageSpeed.setText("0");
            }
            if (maxSpeed == 0) {
                tvMaxSpeed.setText("0");
            }
            if (distance == 0) {
                tvDistance.setText("0");
            }
        }
        if (!isLocationChanged) {
            if (averageSpeed == 0) {
                tvAverageSpeed.setText("-");
            }
            if (maxSpeed == 0) {
                tvMaxSpeed.setText("-");
            }
            if (distance == 0) {
                tvDistance.setText("-");
            }
        }
    }


    private void setIbtChartClick() {
        ibtChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                intent.putExtra("isLocationChanged", isLocationChanged);
                startActivity(intent);
            }
        });
    }


    private void setIbtResetClick() {
        ibtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                averageSpeed = 0;
                maxSpeed = 0;
                distance = 0;
                time = 1;
                dataPoints.clear();

                setDataAfterRotation();
            }
        });
    }


    private void GPS() {
        final DecimalFormat decimalFormat_0 = new DecimalFormat();
        decimalFormat_0.setRoundingMode(RoundingMode.FLOOR);
        decimalFormat_0.setMaximumFractionDigits(0);
        final DecimalFormat decimalFormat_1 = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat_1.setRoundingMode(RoundingMode.FLOOR);
        decimalFormat_1.setMaximumFractionDigits(1);

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {

                if (isLocationChanged) {
                    speed = location.getSpeed();
                    averageSpeed = distance / time;
                }


                if (!isLocationChanged) {
                    lastLatitude = location.getLatitude();
                    lastLongitude = location.getLongitude();
                }
                if (location.getSpeed() != 0) {
                    distance = distance + (float) distance(lastLatitude, lastLongitude, location.getLatitude(), location.getLongitude());
                    lastLatitude = location.getLatitude();
                    lastLongitude = location.getLongitude();
                }


                if (!isLocationChanged) {
                    timer = new Runnable() {
                        @Override
                        public void run() {
                            dataPoints.add(new DataPoint(time, (int) location.getSpeed()));
                            time = time + 1;
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.postDelayed(timer, 0);
                    isLocationChanged = true;
                }


                if (preferences.getInt("unitsState", 1) == 1) {

                    tvSpeed.setText(String.valueOf(decimalFormat_0.format(location.getSpeed() * 3.6)));

                    tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed * 3.6)));

                    if (maxSpeed <= location.getSpeed()) {
                        maxSpeed = location.getSpeed();
                        tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed * 3.6)));
                    }

                    tvDistance.setText(String.valueOf(decimalFormat_1.format(distance / 1000)));

                } else {

                    tvSpeed.setText(String.valueOf(decimalFormat_0.format(location.getSpeed())));

                    tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed)));

                    if (maxSpeed <= location.getSpeed()) {
                        maxSpeed = location.getSpeed();
                        tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed)));
                    }

                    tvDistance.setText(String.valueOf(decimalFormat_1.format(distance)));

                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                tvSpeed.setText("0");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    private double distance(double latStart, double lngStart, double latFinish, double lngFinish) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(latFinish - latStart);
        double dLng = Math.toRadians(lngFinish - lngStart);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latStart)) * Math.cos(Math.toRadians(latFinish)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void GPSAlertDialog() {
        if (!GPSState()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("GPS is disabled");
            alertDialog.setCancelable(false);
            alertDialog.setNeutralButton("BACK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alertDialog.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog.create().show();
        }
    }

    private boolean GPSState() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }


    private void changeFont() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "digital-7.ttf");
        tvSpeed.setTypeface(typeface);
        tvAverageSpeed.setTypeface(typeface);
        tvMaxSpeed.setTypeface(typeface);
        tvDistance.setTypeface(typeface);
    }

    private void setGridLayout() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int halfScreen = (int)(screenWidth * 0.5);

        if (getScreenOrientation() == 1) {
            tvAverageSpeed.setWidth(halfScreen);
            tvAverageSpeedLabel.setWidth(halfScreen);
            tvMaxSpeed.setWidth(halfScreen);
            tvMaxSpeedLabel.setWidth(halfScreen);
            tvDistance.setWidth(halfScreen);
            tvDistanceLabel.setWidth(halfScreen);
        }
        if (getScreenOrientation() == 2) {
            ViewGroup.LayoutParams params = tvAverageSpeed.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            tvAverageSpeed.setLayoutParams(params);
            tvAverageSpeedLabel.setWidth(halfScreen);
        }
    }

    private void setLayoutAfterRotation() {
        if (getScreenOrientation() == 1) {
            tvEmptyBox.setVisibility(View.VISIBLE);
            tvMaxSpeed.setVisibility(View.VISIBLE);
            tvMaxSpeedLabel.setVisibility(View.VISIBLE);
            tvDistance.setVisibility(View.VISIBLE);
            tvDistanceLabel.setVisibility(View.VISIBLE);
        }
        if (getScreenOrientation() == 2) {
            tvEmptyBox.setVisibility(View.GONE);
            tvMaxSpeed.setVisibility(View.GONE);
            tvMaxSpeedLabel.setVisibility(View.GONE);
            tvDistance.setVisibility(View.GONE);
            tvDistanceLabel.setVisibility(View.GONE);

            RelativeLayout.LayoutParams chartLayoutParams = new RelativeLayout.LayoutParams(
                    (int) (30 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (30 * getApplicationContext().getResources().getDisplayMetrics().density));
            chartLayoutParams.setMargins(
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density));
            chartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ibtChart.setLayoutParams(chartLayoutParams);

            RelativeLayout.LayoutParams resetLayoutParams = new RelativeLayout.LayoutParams(
                    (int) (30 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (30 * getApplicationContext().getResources().getDisplayMetrics().density));
            resetLayoutParams.setMargins(
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density),
                    (int) (10 * getApplicationContext().getResources().getDisplayMetrics().density));
            resetLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.grid_layout);
            resetLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.grid_layout);
            ibtReset.setLayoutParams(resetLayoutParams);
        }
    }

    private void setDataAfterRotation() {
        DecimalFormat decimalFormat_1 = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat_1.setRoundingMode(RoundingMode.FLOOR);
        decimalFormat_1.setMaximumFractionDigits(1);

        if (averageSpeed != 0) {
            if (preferences.getInt("unitsState", 1) == 1) {
                tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed * 3.6)));
            } else {
                tvAverageSpeed.setText(String.valueOf(decimalFormat_1.format(averageSpeed)));
            }
        } else {
            tvAverageSpeed.setText("-");
        }

        if (maxSpeed != 0) {
            if (preferences.getInt("unitsState", 1) == 1) {
                tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed * 3.6)));
            } else {
                tvMaxSpeed.setText(String.valueOf(decimalFormat_1.format(maxSpeed)));
            }
        } else {
            tvMaxSpeed.setText("-");
        }

        if (distance != 0) {
            if (preferences.getInt("unitsState", 1) == 1) {
                tvDistance.setText(String.valueOf(decimalFormat_1.format(distance / 1000)));
            } else {
                tvDistance.setText(String.valueOf(decimalFormat_1.format(distance)));
            }
        } else {
            tvDistance.setText("-");
        }
    }

    private void setScreenAlwaysOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private int getScreenOrientation() {
        return this.getResources().getConfiguration().orientation;  // | 1 - 2
    }


}
