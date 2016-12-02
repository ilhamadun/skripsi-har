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
    private List<SensorData> buffer;
    protected SensorDataSequence sensorDataSequence;
    protected SensorDataWriter sensorDataWriter;

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
        createSensorDataSequence(sensorToRead);
        sensorDataWriter = createSensorDataWriter();
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

    public List<SensorData> getLastSensorData() {
        if (sensorDataSequence.size() > 0) {
            return sensorDataSequence.getLastData();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onSensorDataReady() {
        buffer = sensorReader.read();
        if (buffer != null) {
            for (SensorData data : buffer) {
                sensorDataSequence.setData(data);
            }
            sensorDataSequence.commit();

            if (sensorDataSequence.size() % 1500 == 0) {
                writeLog();
                sensorDataSequence.clear();
            }
        }
    }

    protected SensorDataWriter createSensorDataWriter() {
        String basePath = getExternalFilesDir(null).getAbsolutePath();
        String filePath = basePath + "/" + activity + ".csv";
        return new SensorDataWriter(filePath);
    }

    protected void createSensorDataSequence(List<Integer> sensorToRead) {
        sensorDataSequence = new SensorDataSequence();
        for (int sensor : sensorToRead) {
            SensorData sensorData = new SensorData(sensor, 3);
            // TODO: 12/3/2016 Number of axis as extra
            sensorDataSequence.registerSensor(sensorData);
        }
    }

    protected void writeLog() {
        sensorDataWriter.open();
        sensorDataWriter.write(sensorDataSequence);
        sensorDataWriter.close();
    }
}
