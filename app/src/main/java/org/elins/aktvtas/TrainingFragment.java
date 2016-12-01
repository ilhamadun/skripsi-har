package org.elins.aktvtas;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.elins.aktvtas.sensor.LogSensorService;
import org.elins.aktvtas.sensor.SensorData;


public class TrainingFragment extends Fragment {
    private static final String ACTIVITY_ID = "activity_id";
    private static final String TRAINING_DURATION = "training_duration";

    private int activityId;
    private int trainingDuration;
    private int[] sensorToRead = {Sensor.TYPE_ACCELEROMETER}; // TODO: Implement as intent extra

    private LogSensorService logSensorService;
    private boolean logSensorServiceBound = false;

    private CountDownTimer countDownTimer;

    private TextView activityName;
    private TextView timeLeftText;
    private TextView timerContainer;
    private LinearLayout sensorMonitor;

    private List<View> sensorMonitorLayouts = new ArrayList<>();


    public TrainingFragment() {
        // Required empty public constructor
    }

    public static TrainingFragment newInstance(int activityId, int trainingDuration) {
        TrainingFragment fragment = new TrainingFragment();
        Bundle args = new Bundle();
        args.putInt(ACTIVITY_ID, activityId);
        args.putInt(TRAINING_DURATION, trainingDuration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            activityId = getArguments().getInt(ACTIVITY_ID);
            trainingDuration = getArguments().getInt(TRAINING_DURATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        registerViewComponents();
        activityName.setText(activityId);
        startPreparationCountdown(10000);

    }

    private void registerViewComponents() {
        activityName = (TextView) getActivity().findViewById(R.id.trainer_activity_name);
        timeLeftText = (TextView) getActivity().findViewById(R.id.trainer_time_left);
        timerContainer = (TextView) getActivity().findViewById(R.id.trainer_duration);
        sensorMonitor = (LinearLayout) getActivity().findViewById(R.id.sensor_monitor);

        for (int aSensorToRead : sensorToRead) {
            addSensorMonitorLayout(aSensorToRead);
        }

        Button controlButton = (Button) getActivity().findViewById(R.id.trainer_control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                stopTraining();
            }
        });
    }

    private int addSensorMonitorLayout(int sensorId) {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.sensor_monitor,
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
        Intent intent = new Intent(getActivity(), LogSensorService.class);
        intent.putExtra(LogSensorService.LOG_DURATION, trainingDuration);
        intent.putExtra(LogSensorService.SENSOR_TO_READ, sensorToRead);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        startTrainingCountdown();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogSensorService.LogSensorBinder binder = (LogSensorService.LogSensorBinder) service;
            logSensorService = binder.getService();
            logSensorServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logSensorServiceBound = false;
        }
    };

    private void startTrainingCountdown() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        countDownTimer = new CountDownTimer((trainingDuration * 60 * 1000) + 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (logSensorServiceBound) {
                    updateSensorMonitor();
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

        List<SensorData> sensorDataList = logSensorService.getSensorData();

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
            getActivity().unbindService(connection);
            logSensorServiceBound = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTraining();
    }

}
