package org.elins.aktvtas.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorDataSequence {
    List<SensorData> buffer = new ArrayList<>();
    private List<List<SensorData>> sequence = new ArrayList<>();
    HashMap<Integer, Integer> sensorOrder = new HashMap<>();
    private int registeredSensor = 0;

    public SensorDataSequence registerSensor(SensorData sensorData) {
        sensorOrder.put(sensorData.sensorType(), registeredSensor++);
        buffer.add(sensorData);

        return this;
    }

    public SensorDataSequence setData(SensorData sensorData) {
        int index = sensorOrder.get(sensorData.sensorType());

        SensorData newSensorData = new SensorData(sensorData.sensorType(), sensorData.numberOfAxis());
        newSensorData.setValues(sensorData.getValues());
        buffer.set(index, newSensorData);

        return this;
    }

    public void commit() {
        if (! dataCorrupted()) {
            sequence.add(buffer);
        }
        resetBuffer();
    }

    private boolean dataCorrupted() {
        for (SensorData sensorData : buffer) {
            for (Float data : sensorData.getValues()) {
                if (data == null) {
                    return true;
                }
            }
        }

        return false;
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

    public List<SensorData> getLastData() {
        return sequence.get(sequence.size() - 1);
    }

    public SensorData getLastData(SensorData sensorData) {
        int index = sensorOrder.get(sensorData.sensorType());

        List<SensorData> lastData = sequence.get(sequence.size() - 1);
        return lastData.get(index);
    }

    public List<SensorData> getDataByIndex(int index) {
        return sequence.get(index);
    }

    public List<List<SensorData>> getAll() {
        return sequence;
    }

    public float[] flatten() {
        List<List<Float>> sequenceMatrix = matrix();
        float[] flattenedSequence = new float[sequenceMatrix.size() * sequenceMatrix.get(0).size()];

        int idx = 0;
        for (List<Float> values : sequenceMatrix) {
            for (Float v : values) {
                flattenedSequence[idx++] = v;
            }
        }

        return  flattenedSequence;
    }

    public List<List<Float>> matrix() {
        List<List<Float>> flattenedSequence = new ArrayList<>();

        for (List<SensorData> sensorDatas : sequence) {
            List<Float> flattenedSensors = flattenSensors(sensorDatas);
            flattenedSequence.add(flattenedSensors);
        }

        return flattenedSequence;
    }

    private List<Float> flattenSensors(List<SensorData> sensorDatas) {
        List<Float> flattenedSensors = new ArrayList<>();

        for (SensorData sensorData : sensorDatas) {
            flattenedSensors.addAll(sensorData.getValues());
        }

        return flattenedSensors;
    }

    public void clear() {
        sequence.clear();
    }
}
