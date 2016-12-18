package org.elins.aktvtas.sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.elins.aktvtas.sensor.SensorData;
import org.elins.aktvtas.sensor.SensorDataSequence;
import org.elins.aktvtas.sensor.SensorReader;

import java.util.ArrayList;
import java.util.List;

public class SensorService extends Service implements SensorReader.SensorReaderEvent {
    public static final String SENSOR_TO_READ = "org.elins.aktvtas.extra.SENSOR_TO_READ";

    protected List<Integer> sensorToRead = new ArrayList<>();
    protected List<Integer> numberOfAxis = new ArrayList<>();

    protected String logType = "BASE";

    protected SensorReader sensorReader;
    protected SensorDataSequence sensorDataSequence;
    protected SensorDataWriter sensorDataWriter;

    private String filePath;
    private List<SensorData> buffer;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Should be used as base class only");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        writeBuffer();
        return false;
    }

    @Override
    public void onSensorDataReady() {
        buffer = sensorReader.read();
        if (buffer != null) {
            for (SensorData data : buffer) {
                sensorDataSequence.setData(data);
            }
            sensorDataSequence.commit();
        }
    }

    protected void writeBuffer() {
        writeLog();
        sensorDataSequence.clear();
    }

    public List<SensorData> getLastSensorData() {
        if (sensorDataSequence.size() > 0) {
            return sensorDataSequence.getLastData();
        } else {
            return new ArrayList<>();
        }
    }

    protected void extractSensorToRead(int[] sensors) {
        for (int sensor : sensors) {
            sensorToRead.add(sensor);
            numberOfAxis.add(3);
        }
    }

    protected void createSensorDataReader(List<Integer> sensorToRead) {
        sensorReader = new SensorReader(this, sensorToRead);
        sensorReader.enableEventCallback(this);
    }

    protected void createSensorDataSequence(List<Integer> sensorToRead, List<Integer> numberOfAxis) {
        sensorDataSequence = new SensorDataSequence();
        for (int i = 0; i < sensorToRead.size(); i++) {
            SensorData sensorData = new SensorData(sensorToRead.get(i), numberOfAxis.get(i));
            sensorDataSequence.registerSensor(sensorData);
        }
    }

    protected void createSensorDataWriter(String filename) {
        String basePath = getExternalFilesDir(null).getAbsolutePath();
        filePath = basePath + "/" + filename + ".csv";
        sensorDataWriter = new SensorDataWriter(filePath);
    }

    protected void writeLog() {
        int numberOfSensors = sensorToRead.size();
        int totalSensorAxis = 0;
        for (Integer s : numberOfAxis) {
            totalSensorAxis += s;
        }

        sensorDataWriter.open();
        sensorDataWriter.write(logType, numberOfSensors, totalSensorAxis, sensorDataSequence);
        sensorDataWriter.close();
    }

    public String getFilePath() {
        return filePath;
    }
}
