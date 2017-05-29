package org.elins.aktvtas;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.elins.aktvtas.human.HumanActivityHistory;
import org.elins.aktvtas.human.HumanActivityHistoryAdapter;
import org.elins.aktvtas.human.Recognition;

import java.util.ArrayList;
import java.util.List;


public class PredictionActivity extends AppCompatActivity {
    private static final int WINDOW_SIZE = 100;
    private static final float OVERLAP = 0.5f;
    private static final int[] SENSOR_TO_READ = {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
                                                 Sensor.TYPE_LINEAR_ACCELERATION};

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

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PredictionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        predictionIcon = (ImageView) findViewById(R.id.prediction_icon);
        predictionName = (TextView) findViewById(R.id.prediction_name);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment activityHistoryFragment = ActivityHistoryFragment.newInstance(10);
        transaction.add(R.id.activity_prediction_history, activityHistoryFragment).commit();

        startPredictionService();
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

    public void startPredictionService() {
        Intent intent = new Intent(this, PredictionService.class);
        intent.putExtra(PredictionService.WINDOW_SIZE, WINDOW_SIZE);
        intent.putExtra(PredictionService.OVERLAP, OVERLAP);
        intent.putExtra(PredictionService.SENSOR_TO_READ, SENSOR_TO_READ);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PredictionService.BROADCAST_ACTION);
        broadcastManager.registerReceiver(predictionReceiver, intentFilter);
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

}
