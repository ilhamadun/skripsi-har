package org.elins.aktvtas;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.elins.aktvtas.sensor.DataAcquisition;
import org.elins.aktvtas.human.Recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PredictionActivity extends AppCompatActivity
        implements CountDownFragment.OnCountDownListener {
    private static final int WINDOW_SIZE = 100;
    private static final float OVERLAP = 0.5f;
    private static final int[] SENSOR_TO_READ = {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
                                                 Sensor.TYPE_LINEAR_ACCELERATION};
    private static final long PREPARATION_TIME = 10000;

    private CountDownFragment countDownFragment;
    private CountDownTimer predictionCountDown;

    private DataAcquisition acquisition;

    private ImageView predictionIcon;
    private TextView predictionName;

    private PredictionService predictionService;
    private boolean predictionServiceBound = false;

    private BroadcastReceiver predictionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PredictionService.BROADCAST_ACTION)) {
                int ids[] = intent.getIntArrayExtra(PredictionService.PREDICTION_RESULT_ID);
                float confidences[] = intent.getFloatArrayExtra(
                        PredictionService.PREDICTION_RESULT_CONFIDENCE);

                List<Recognition> recognitions = new ArrayList<>();
                for (int i = 0; i < ids.length; i++) {
                    recognitions.add(new Recognition(ids[i], confidences[i]));
                }

                updatePrediction(recognitions);
            }
        }
    };

    private LocalBroadcastManager broadcastManager;

    public static void startActivity(Context context, int activityId, int sensorPlacement,
                                     int duration) {
        Intent intent = new Intent(context, PredictionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DataAcquisition.ACTIVITY_ID, activityId);
        intent.putExtra(DataAcquisition.SENSOR_PLACEMENT, sensorPlacement);
        intent.putExtra(DataAcquisition.ACQUISITION_DURATION, duration);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        acquisition = new DataAcquisition(getIntent());

        setContentView(R.layout.activity_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        countDownFragment = CountDownFragment.newInstance(PREPARATION_TIME);
        transaction.replace(R.id.prediction_countdown, countDownFragment).commit();

        predictionIcon = (ImageView) findViewById(R.id.prediction_icon);
        predictionName = (TextView) findViewById(R.id.prediction_name);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PredictionService.PredictionBinder binder = (PredictionService.PredictionBinder) service;
            predictionService = binder.getService();
            predictionServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            predictionServiceBound = false;
        }
    };

    public void startPrediction() {
        Intent intent = new Intent(this, PredictionService.class);
        intent.putExtra(PredictionService.WINDOW_SIZE, WINDOW_SIZE);
        intent.putExtra(PredictionService.OVERLAP, OVERLAP);
        intent.putExtra(PredictionService.SENSOR_TO_READ, SENSOR_TO_READ);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PredictionService.BROADCAST_ACTION);
        broadcastManager.registerReceiver(predictionReceiver, intentFilter);

        startPredictionCountdown();
    }

    private void startPredictionCountdown() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        predictionCountDown = new CountDownTimer((acquisition.getDuration() * 1000) + 1000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished - 1000);
                countDownFragment.updateTimeLeft(dateFormat.format(date));
            }

            @Override
            public void onFinish() {
                stopPredictionService();
            }
        }.start();
    }

    public void stopPredictionService() {
        if (predictionServiceBound) {
            unbindService(connection);
            predictionServiceBound = false;

            broadcastManager.unregisterReceiver(predictionReceiver);
        }
    }

    public void updatePrediction(List<Recognition> recognitions) {
        if (recognitions == null || recognitions.size() == 0) {
            predictionIcon.setImageResource(R.drawable.ic_not_recognized);
            predictionName.setText(R.string.not_recognized);
        } else  {
            Recognition best = recognitions.get(0);
            predictionIcon.setImageResource(best.getIcon());
            predictionName.setText(best.getName());
            Log.i("PredictionService", String.format("Activity: %s\tConfidence: %f", getString(best.getName()), best.getConfidence()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPredictionService();
    }

    @Override
    public void onPreparationFinish() {
        startPrediction();
    }

}
