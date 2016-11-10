package elins.org.aktvtas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorDataSequence {
    protected List<SensorData> buffer = new ArrayList<>();
    private List<List<SensorData>> sequence = new ArrayList<>();
    protected HashMap<SensorData, Integer> sensorOrder = new HashMap<>();
    private int registeredSensor = 0;

    public SensorDataSequence registerSensor(SensorData sensorData) {
        sensorOrder.put(sensorData, registeredSensor++);
        buffer.add(sensorData);

        return this;
    }

    public SensorDataSequence setData(SensorData sensorData) {
        int index = sensorOrder.get(sensorData);

        SensorData newSensorData = new SensorData(sensorData.sensorType(), sensorData.numberOfAxis());
        newSensorData.setValues(sensorData.getValues());
        buffer.set(index, newSensorData);

        return this;
    }

    public void commit() {
        sequence.add(buffer);
        resetBuffer();
    }

    private void resetBuffer() {
        List<SensorData> oldBuffer = buffer;
        buffer = new ArrayList<>();

        for (SensorData sensorData : oldBuffer) {
            buffer.add(new SensorData(sensorData.sensorType(), sensorData.numberOfAxis()));
        }
    }

    public int size() {
        return sequence.size();
    }

    public SensorData getLastData(SensorData sensorData) {
        int index = sensorOrder.get(sensorData);

        List<SensorData> lastData = sequence.get(sequence.size() - 1);
        return lastData.get(index);
    }

    public List<SensorData> getDataByIndex(int index) {
        return sequence.get(index);
    }

    public List<List<SensorData>> getAll() {
        return sequence;
    }

    public List<List<Double>> flatten() {
        List<List<Double>> flattenedSequence = new ArrayList<>();

        for (List<SensorData> sensorDatas : sequence) {
            List<Double> flattenedSensors = flattenSensors(sensorDatas);
            flattenedSequence.add(flattenedSensors);
        }

        return flattenedSequence;
    }

    private List<Double> flattenSensors(List<SensorData> sensorDatas) {
        List<Double> flattenedSensors = new ArrayList<>();

        for (SensorData sensorData : sensorDatas) {
            flattenedSensors.addAll(sensorData.getValues());
        }

        return flattenedSensors;
    }

    public void clear() {
        sequence.clear();
    }
}
