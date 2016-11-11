package elins.org.aktvtas.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorReader implements SensorEventListener {

    private SensorManager sensorManager;
    private List<Sensor> availableSensors = new ArrayList<>();
    private List<SensorData> sensorDataBuffer;

    public SensorReader(Context context, List<Integer> sensorToRead) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        for (Integer sensorType : sensorToRead) {
            registerSensorIfAvailable(sensorType);
        }
        resetBuffer();
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

    public List<Sensor> getAvailableSensor() {
        return availableSensors;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        storeAvailableSensorDataToBuffer(event);
    }

    protected void storeAvailableSensorDataToBuffer(SensorEvent event) {
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
            List<SensorData> sensorDatas = sensorDataBuffer;
            resetBuffer();
            return sensorDatas;
        } else {
            return null;
        }
    }
}
