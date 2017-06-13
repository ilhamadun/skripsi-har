package org.elins.aktvtas;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.human.Recognition;
import org.elins.aktvtas.sensor.DataAcquisition;
import org.elins.aktvtas.sensor.PredictionLogWriter;
import org.elins.aktvtas.sensor.SensorService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionService extends SensorService {
    private static final String TAG = "PredictionService";

    public static final String EXTRA_WINDOW_SIZE = "org.elins.aktvtas.extra.EXTRA_WINDOW_SIZE";
    public static final String EXTRA_OVERLAP = "org.elins.aktvtas.extra.EXTRA_OVERLAP";

    private static final String BASE_ACTION = "org.elins.aktvtas.human.";
    public static final String BROADCAST_ACTION = BASE_ACTION + "BROADCAST_PREDICTION_SERVICE";
    public static final String PREDICTION_RESULT_ID = BASE_ACTION + "PREDICTION_SERVICE_RESULT_ID";
    public static final String PREDICTION_RESULT_CONFIDENCE =
            BASE_ACTION + "PREDICTION_SERVICE_RESULT_CONFIDENCE";
    public static final String PREDICTION_ACCURACY = BASE_ACTION + "PREDICTION_SERVICE_ACCURACY";
    public static final String TOTAL_PREDICTION = BASE_ACTION + "PREDICTION_SERVICE_TOTAL";
    public static final String CORRECT_PREDICTION = BASE_ACTION + "PREDICTION_SERVICE_CORRECT";
    public static final String PREDICTION_TIME = BASE_ACTION + "PREDICTION_SERVICE_TIME";
    
    public static final int DEFAULT_WINDOW_SIZE = 100;
    public static final float DEFAULT_OVERLAP = 0.5f;

    public PredictionService() {
    }
    
    public  class PredictionBinder extends Binder {
        public PredictionService getService() {
            return PredictionService.this;
        }
    }

    private DataAcquisition acquisition;
    private int windowSize;
    private float overlap;

    private HumanActivityClassifier classifier;
    private List<Recognition> lastRecognitions;
    private int totalPrediction = 0;
    private int correctPrediction = 0;
    private long totalPredictionTime = 0;
    private float accuracy = 0f;

    private PredictionLogWriter logWriter;
    
    private final IBinder binder = new PredictionBinder();

    @Override
    public IBinder onBind(Intent intent) {
        logType = "PREDICTION";

        acquisition = new DataAcquisition(intent);
        windowSize = intent.getIntExtra(EXTRA_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        overlap = intent.getFloatExtra(EXTRA_OVERLAP, DEFAULT_OVERLAP);
        int[] sensors = intent.getIntArrayExtra(EXTRA_SENSOR_TO_READ);
        
        extractSensorToRead(sensors);
        createSensorDataSequence(sensorToRead, numberOfAxis);
        createPredictionLogWriter(generateFilename(new Date()));
        createSensorDataReader(sensorToRead);

        classifier = new HumanActivityClassifier(getAssets());
        
        return binder;
    }

    private String generateFilename(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.getDefault());
        return String.format("Prediction-%s", dateFormat.format(date));
    }

    protected void createPredictionLogWriter(String filename) {
        String basePath = getExternalFilesDir(null).getAbsolutePath();
        filePath = basePath + "/" + filename + ".csv";
        logWriter = new PredictionLogWriter(filePath);

        logWriter.open();
        logWriter.writeMetadata(acquisition.getActivityId().ordinal(),
                acquisition.getSensorPlacement(), sensorToRead.size());
    }

    @Override
    public void onSensorDataReady() {
        super.onSensorDataReady();

        if (sensorDataSequence.size() == windowSize) {
            long startTime = System.currentTimeMillis();

            List<Recognition> recognitions = classifier.classify(sensorDataSequence);

            long predictionTime = System.currentTimeMillis() - startTime;
            totalPredictionTime += predictionTime;

            if (! recognitions.equals(lastRecognitions)) {
                reportPredictions(recognitions);
                lastRecognitions = recognitions;
            }

            totalPrediction += 1;

            if (recognitions.size() > 0) {
                if (recognitions.get(0).getId() == acquisition.getActivityId().ordinal()) {
                    correctPrediction += 1;
                }

                if (correctPrediction > 0) {
                    accuracy = ((float) correctPrediction / (float) totalPrediction) * 100;
                }

                Log.i(TAG, String.format("Prediction: %d, Expected: %d", lastRecognitions.get(0).getId(),
                        acquisition.getActivityId().ordinal()));
            }

            Log.i(TAG, String.format("Total: %d, Correct: %d, Accuracy: %f", totalPrediction,
                    correctPrediction, accuracy));
            Log.i(TAG, String.format("Prediction time: %dms", predictionTime));

            logWriter.write(recognitions.get(0).getId(), predictionTime);
            rearrangeSequence();
        }
    }

    private void rearrangeSequence() {
        int fromIndex = Math.round(windowSize * (1 - overlap));
        try {
            sensorDataSequence.slice(fromIndex, windowSize);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Failed to rearrange SensorDataSequence.");
        }
    }

    private void reportPredictions(List<Recognition> recognitions) {
        int ids[] = new int[recognitions.size()];
        float confidences[] = new float[recognitions.size()];
        long predictionTimeAvg = 0;

        for (int i = 0; i < recognitions.size(); i++) {
            ids[i] = recognitions.get(i).getId();
            confidences[i] = recognitions.get(i).getConfidence();
        }

        if (totalPrediction > 0) {
            predictionTimeAvg = totalPredictionTime / totalPrediction;
        }
        Log.i(TAG, String.format("Average prediction time: %dms", predictionTimeAvg));

        Intent intent = new Intent(BROADCAST_ACTION)
                .putExtra(PREDICTION_RESULT_ID, ids)
                .putExtra(PREDICTION_RESULT_CONFIDENCE, confidences)
                .putExtra(PREDICTION_ACCURACY, accuracy)
                .putExtra(TOTAL_PREDICTION, totalPrediction)
                .putExtra(CORRECT_PREDICTION, correctPrediction)
                .putExtra(PREDICTION_TIME, predictionTimeAvg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        if (logWriter != null) {
            logWriter.close();
        }
        sensorReader.close();
    }
}
