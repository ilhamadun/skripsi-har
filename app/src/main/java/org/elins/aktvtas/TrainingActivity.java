package org.elins.aktvtas;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.elins.aktvtas.human.HumanActivity;
import org.elins.aktvtas.sensor.LogSensorService;
import org.elins.aktvtas.sensor.SensorData;


public class TrainingActivity extends AppCompatActivity {
    public static final String ACTIVITY_ID = "org.elins.aktvtas.extra.ACTIVITY_ID";
    public static final String TRAINING_POSITION = "org.elins.aktvtas.extra.TRAINING_POSITION";
    public static final String TRAINING_DURATION = "org.elins.aktvtas.extra.TRAINING_DURATION";
    public static final String RESULT = "org.elins.aktvtas.extra.TRAINING_RESULT";

    public static final int REQUEST_CODE_TRAINING_ACTIVITY = 1;

    private HumanActivity.Id activityId;
    private CharSequence trainingPosition;
    private int trainingDurationSecond;
    private int[] sensorToRead = {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE}; // TODO: Implement as intent extra
    private String filePath = null;

    private LogSensorService logSensorService;
    private boolean logSensorServiceBound = false;
    private boolean doubleBackToExitPressedOnce;

    private CountDownTimer countDownTimer;

    private TextView activityName;
    private TextView timeLeftText;
    private TextView timerContainer;
    private LinearLayout sensorMonitor;

    private List<View> sensorMonitorLayouts = new ArrayList<>();


    public TrainingActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        activityId = HumanActivity.Id.valueOf(intent.getIntExtra(ACTIVITY_ID, 0));
        trainingPosition = intent.getCharSequenceExtra(TRAINING_POSITION);
        trainingDurationSecond = intent.getIntExtra(TRAINING_DURATION, 600);

        filterUnavailableSensor();

        setContentView(R.layout.activity_training);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerViewComponents();

        HumanActivity humanActivity = new HumanActivity(activityId);
        activityName.setText(humanActivity.name());

        if (! logSensorServiceBound) {
            startPreparationCountdown(10000);
        }
    }

    private void filterUnavailableSensor() {
        List<Integer> filtered = new ArrayList<>();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for (int aSensorToRead : sensorToRead) {
            if (sensorManager.getDefaultSensor(aSensorToRead) != null) {
                filtered.add(aSensorToRead);
            }
        }

        sensorToRead = new int[filtered.size()];

        for (int i = 0; i < filtered.size(); i++) {
            sensorToRead[i] = filtered.get(i);
        }
    }

    private void registerViewComponents() {
        activityName = (TextView) findViewById(R.id.trainer_activity_name);
        timeLeftText = (TextView) findViewById(R.id.trainer_time_left);
        timerContainer = (TextView) findViewById(R.id.trainer_duration);
        sensorMonitor = (LinearLayout) findViewById(R.id.sensor_monitor);

        for (int aSensorToRead : sensorToRead) {
            addSensorMonitorLayout(aSensorToRead);
        }

        Button controlButton = (Button) findViewById(R.id.trainer_control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                stopTraining();
            }
        });
    }

    private int addSensorMonitorLayout(int sensorId) {
        View layout = LayoutInflater.from(this).inflate(R.layout.sensor_monitor,
                sensorMonitor, false);

        String sensorName = getSensorName(sensorId);

        sensorMonitor.addView(layout);
        TextView title = (TextView) layout.findViewById(R.id.sensor_monitor_title);
        title.setText(sensorName);

        sensorMonitorLayouts.add(layout);

        return sensorMonitorLayouts.size() - 1;
    }

    private String getSensorName(int sensorId) {
        switch (sensorId) {
            case Sensor.TYPE_ACCELEROMETER:
                return getResources().getString(R.string.accelerometer);
            case Sensor.TYPE_GYROSCOPE:
                return getResources().getString(R.string.gyroscope);
        }

        return String.valueOf(sensorId);
    }

    private void startPreparationCountdown(long preparationTimeInMilliseconds) {
        countDownTimer = new CountDownTimer(preparationTimeInMilliseconds + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerContainer.setText(String.valueOf((millisUntilFinished - 1000) / 1000));
            }

            @Override
            public void onFinish() {
                timeLeftText.setText(R.string.time_left);
                startTraining();
            }
        }.start();
    }

    private void startTraining() {
        Intent intent = new Intent(this, LogSensorService.class);
        intent.putExtra(LogSensorService.ACTIVITY_ID, activityId.ordinal());
        intent.putExtra(LogSensorService.SENSOR_POSITION, trainingPosition);
        intent.putExtra(LogSensorService.LOG_DURATION_SECOND, trainingDurationSecond);
        intent.putExtra(LogSensorService.SENSOR_TO_READ, sensorToRead);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        startTrainingCountdown();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogSensorService.LogSensorBinder binder = (LogSensorService.LogSensorBinder) service;
            logSensorService = binder.getService();
            logSensorServiceBound = true;

            filePath = logSensorService.getFilePath();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logSensorServiceBound = false;
        }
    };

    private void startTrainingCountdown() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        countDownTimer = new CountDownTimer((trainingDurationSecond * 1000) + 1000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (logSensorServiceBound) {
                    updateSensorMonitor();
                    logSensorService.updateNotification(millisUntilFinished);
                }

                Date date = new Date(millisUntilFinished - 1000);
                timerContainer.setText(dateFormat.format(date));
            }

            @Override
            public void onFinish() {
                stopTraining();
            }
        }.start();
    }

    private void updateSensorMonitor() {
        if (logSensorService == null) {
            return;
        }

        List<SensorData> sensorDataList = logSensorService.getLastSensorData();

        if (sensorDataList.size() > 0) {
            for (SensorData sensorData : sensorDataList) {
                View monitor;
                if (sensorData.sensorType() == Sensor.TYPE_ACCELEROMETER) {
                    monitor = sensorMonitorLayouts.get(0);
                } else {
                    monitor = sensorMonitorLayouts.get(1);
                }

                TextView x = (TextView) monitor.findViewById(R.id.sensor_monitor_x);
                TextView y = (TextView) monitor.findViewById(R.id.sensor_monitor_y);
                TextView z = (TextView) monitor.findViewById(R.id.sensor_monitor_z);

                Locale locale = Locale.getDefault();
                x.setText(String.format(locale, "%f", sensorData.getAxisValue(0)));
                y.setText(String.format(locale, "%f", sensorData.getAxisValue(1)));
                z.setText(String.format(locale, "%f", sensorData.getAxisValue(2)));
            }
        }
    }

    private void stopTraining() {
        if (logSensorServiceBound) {
            unbindService(connection);
            logSensorServiceBound = false;
            createResultIntent();
            finish();
        }
    }

    private void createResultIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT, filePath);

        if (filePath == null) {
            setResult(RESULT_CANCELED, returnIntent);
        } else {
            setResult(Activity.RESULT_OK, returnIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            stopTraining();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_again_to_stop_training, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTraining();
    }

}
