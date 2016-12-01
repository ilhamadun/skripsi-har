package org.elins.aktvtas.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SensorReader implements SensorEventListener {

    public interface SensorReaderEvent {
        void onSensorDataReady();
    }

    private final SensorManager sensorManager;
    private List<Sensor> availableSensors = new ArrayList<>();
    private List<SensorData> sensorDataBuffer;
    private SensorReaderEvent sensorReaderEvent;

    public SensorReader(Context context, List<Integer> sensorToRead) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        for (Integer sensorType : sensorToRead) {
            registerSensorIfAvailable(sensorType);
        }
        resetBuffer();
    }

    public void enableEventCallback(SensorReaderEvent event) {
        sensorReaderEvent = event;
    }

    private void registerSensorIfAvailable(int sensorType) {
        if (sensorManager.getDefaultSensor(sensorType) != null) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            availableSensors.add(sensor);
        }
    }

    private void resetBuffer() {
        SensorData[] initialSensorDataBuffer = new SensorData[availableSensors.size()];
        sensorDataBuffer = Arrays.asList(initialSensorDataBuffer);
    }

    public void close() {
        sensorManager.unregisterListener(this);
    }

    public List<Sensor> getAvailableSensor() {
        return availableSensors;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        storeAvailableSensorDataToBuffer(event);

        if (readyToRead() && sensorReaderEvent != null) {
            sensorReaderEvent.onSensorDataReady();
        }
    }

    void storeAvailableSensorDataToBuffer(SensorEvent event) {
        if (availableSensors.contains(event.sensor)) {
            int bufferIndex = availableSensors.indexOf(event.sensor);
            SensorData sensorData = new SensorData(event.sensor.getType(), event.values.length);
            sensorData.setValues(event.values);
            sensorDataBuffer.set(bufferIndex, sensorData);
        }
    }

    public boolean readyToRead() {
        for (SensorData buffer : sensorDataBuffer) {
            if (buffer == null) {
                return false;
            }
        }
        return true;
    }

    public List<SensorData> read() {
        if (readyToRead()) {
            List<SensorData> sensorDataList = new ArrayList<>(sensorDataBuffer);
            resetBuffer();
            return sensorDataList;
        } else {
            return new ArrayList<>();
        }
    }
}
