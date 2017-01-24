package org.elins.aktvtas.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public static HashMap<String, Boolean> listAllAvailableSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(
                Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        HashMap<String, Boolean> sensorMap = new HashMap<>();

        sensorMap.put("accelerometer", false);
        sensorMap.put("ambient_temperature", false);
        sensorMap.put("gravity", false);
        sensorMap.put("gyroscope", false);
        sensorMap.put("light", false);
        sensorMap.put("linear_accelerometer", false);
        sensorMap.put("magnetic_field", false);
        sensorMap.put("orientation", false);
        sensorMap.put("pressure", false);
        sensorMap.put("proximity", false);
        sensorMap.put("relative_humidity", false);
        sensorMap.put("rotation_vector", false);
        sensorMap.put("temperature", false);

        for (Sensor sensor : deviceSensors) {
            switch (sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    sensorMap.put("accelerometer", true);
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    sensorMap.put("ambient_temperature", true);
                    break;
                case Sensor.TYPE_GRAVITY:
                    sensorMap.put("gravity", true);
                    break;
                case Sensor.TYPE_LIGHT:
                    sensorMap.put("light", true);
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    sensorMap.put("linear_accelerometer", true);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    sensorMap.put("magnetic_field", true);
                    break;
                case Sensor.TYPE_PRESSURE:
                    sensorMap.put("pressure", true);
                    break;
                case Sensor.TYPE_PROXIMITY:
                    sensorMap.put("proximity", true);
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    sensorMap.put("relative_humidity", true);
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    sensorMap.put("rotation_vector", true);
                    break;
            }
        }

        return sensorMap;
    }
}
