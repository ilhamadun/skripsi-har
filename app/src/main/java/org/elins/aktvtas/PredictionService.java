package org.elins.aktvtas;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.elins.aktvtas.human.HumanActivity;
import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.sensor.SensorDataSequence;
import org.elins.aktvtas.sensor.SensorService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PredictionService extends SensorService {
    public static final String WINDOW_SIZE = "org.elins.aktvtas.extra.WINDOW_SIZE";
    public static final String OVERLAP = "org.elins.aktvtas.extra.OVERLAP";
    
    private static final int DEFAULT_WINDOW_SIZE = 100;
    private static final float DEFAULT_OVERLAP = 0.5f;

    public PredictionService() {
    }
    
    public  class PredictionBinder extends Binder {
        public PredictionService getService() {
            return PredictionService.this;
        }
    }
    
    private int windowSize;
    private float overlap;

    private HumanActivityClassifier classifier = new HumanActivityClassifier();
    
    private final IBinder binder = new PredictionBinder();

    @Override
    public IBinder onBind(Intent intent) {
        logType = "PREDICTION";

        windowSize = intent.getIntExtra(WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        overlap = intent.getFloatExtra(OVERLAP, DEFAULT_OVERLAP);
        int[] sensors = intent.getIntArrayExtra(SENSOR_TO_READ);
        
        extractSensorToRead(sensors);
        createSensorDataSequence(sensorToRead, numberOfAxis);
        createSensorDataWriter(generateFilename(new Date()));
        createSensorDataReader(sensorToRead);
        
        return binder;
    }

    private String generateFilename(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.getDefault());
        return String.format("Prediction-%s", dateFormat.format(date));
    }

    @Override
    public void onSensorDataReady() {
        super.onSensorDataReady();
        classify();
        writeBuffer();
    }

    public interface  PredictionEvent {
        void onNewPredictionReady(HumanActivity.Id activityId);
    }

    private PredictionEvent predictionEvent;

    public void enableEventCallback(PredictionEvent event) {
        predictionEvent = event;
    }

    private void classify() {
        if (sensorDataSequence.size() % windowSize == 0) {
            SensorDataSequence sequence = createPredictionWindow();
            int activity = classifier.classify(sequence);
            predictionEvent.onNewPredictionReady(HumanActivity.Id.valueOf(activity));
        }
    }

    private SensorDataSequence createPredictionWindow() {
        // TODO: 12/18/2016 Implement method to create a sensor data window for predicting activity
        return new SensorDataSequence();
    }
}
