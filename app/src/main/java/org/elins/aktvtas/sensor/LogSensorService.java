package org.elins.aktvtas.sensor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class LogSensorService extends Service implements SensorReader.SensorReaderEvent {
    public static final String ACTIVITY = "org.elins.aktvtas.extra.ACTIVITY";
    public static final String LOG_DURATION = "org.elins.aktvtas.extra.LOG_DURATION";
    public static final String SENSOR_TO_READ = "org.elins.aktvtas.extra.SENSOR_TO_READ";

    private static final long DEFAULT_LOG_DURATION_IN_SECONDS = 600;

    String activity;
    long logDurationInSeconds;
    private SensorReader sensorReader;
    private SensorDataSequence sensorDataSequence;
    private SensorDataWriter sensorDataWriter;

    private final IBinder binder = new LogSensorBinder();

    public class LogSensorBinder extends Binder {
        public LogSensorService getService() {
            return LogSensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        activity = intent.getStringExtra(ACTIVITY);
        logDurationInSeconds = intent.getLongExtra(LOG_DURATION, DEFAULT_LOG_DURATION_IN_SECONDS);
        int[] sensors = intent.getIntArrayExtra(SENSOR_TO_READ);

        List<Integer> sensorToRead = new ArrayList<>();
        for (int sensor : sensors) {
            sensorToRead.add(sensor);
        }
        sensorReader = new SensorReader(this, sensorToRead);
        sensorReader.enableEventCallback(this);

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        if (sensorDataWriter != null) {
            sensorDataWriter.close();
        }
        sensorReader.close();
    }

    public List<SensorData> getSensorData() {
        return sensorReader.read();
    }

    @Override
    public void onSensorDataReady() {
        // TODO: 12/1/2016 Implement sensor data logging.
    }

    private SensorDataWriter createSensorDataWriter() {
        String basePath = getFilesDir().getAbsolutePath();
        String filePath = basePath + "/" + activity + ".csv";
        return new SensorDataWriter(filePath);
    }

    private SensorDataSequence createSensorDataSequence() {
        while (! sensorReader.readyToRead()) {
            // Wait to read first sensor data
        }
        List<SensorData> sensorDatas = sensorReader.read();
        SensorDataSequence sensorDataSequence = new SensorDataSequence();
        for (SensorData sensorData : sensorDatas) {
            sensorDataSequence.registerSensor(sensorData);
        }

        return sensorDataSequence;
    }
}
