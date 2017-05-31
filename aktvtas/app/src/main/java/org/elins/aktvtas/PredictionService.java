package org.elins.aktvtas;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.human.Recognition;
import org.elins.aktvtas.sensor.SensorService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionService extends SensorService {
    public static final String WINDOW_SIZE = "org.elins.aktvtas.extra.WINDOW_SIZE";
    public static final String OVERLAP = "org.elins.aktvtas.extra.OVERLAP";
    public static final String BROADCAST_ACTION =
            "org.elins.aktvtas.human.BROADCAST_PREDICTION_SERVICE";
    public static final String PREDICTION_RESULT_ID =
            "org.elins.aktvtas.human.PREDICTION_SERVICE_RESULT_ID";
    public static final String PREDICTION_RESULT_CONFIDENCE =
            "org.elins.aktvtas.human.PREDICTION_SERVICE_RESULT_CONFIDENCE";
    
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

    private HumanActivityClassifier classifier;
    private List<Recognition> lastRecognitions;
    
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

//        classifier = new HumanActivityClassifier(getAssets());
        
        return binder;
    }

    private String generateFilename(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.getDefault());
        return String.format("Prediction-%s", dateFormat.format(date));
    }

    @Override
    public void onSensorDataReady() {
        super.onSensorDataReady();

        if (sensorDataSequence.size() == windowSize) {
//            List<Recognition> recognitions = classifier.classify(sensorDataSequence);
//
//            if (! recognitions.equals(lastRecognitions)) {
//                reportPredictions(recognitions);
//                lastRecognitions = recognitions;
//            }
//
//            writeBuffer();
        }
    }

    @Override
    protected void writeBuffer() {
        writeLog();
        rearrangeSequence();
    }

    private void rearrangeSequence() {
        int fromIndex = Math.round(windowSize * (1 - overlap));
        sensorDataSequence.slice(fromIndex, windowSize);
    }

    private void reportPredictions(List<Recognition> recognitions) {
        int ids[] = new int[recognitions.size()];
        float confidences[] = new float[recognitions.size()];

        for (int i = 0; i < recognitions.size(); i++) {
            ids[i] = recognitions.get(i).getId();
            confidences[i] = recognitions.get(i).getConfidence();
        }

        Intent intent = new Intent(BROADCAST_ACTION)
                .putExtra(PREDICTION_RESULT_ID, ids)
                .putExtra(PREDICTION_RESULT_CONFIDENCE, confidences);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
