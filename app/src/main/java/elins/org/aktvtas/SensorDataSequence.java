package elins.org.aktvtas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorDataSequence {
    protected List<Sensor> buffer = new ArrayList<>();
    private List<List<Sensor>> sequence = new ArrayList<>();
    protected HashMap<Sensor, Integer> sensorOrder = new HashMap<>();
    private int registeredSensor = 0;

    public SensorDataSequence registerSensor(Sensor sensor) {
        sensorOrder.put(sensor, registeredSensor++);
        buffer.add(sensor);

        return this;
    }

    public SensorDataSequence setData(Sensor sensor) {
        int index = sensorOrder.get(sensor);

        Sensor newSensor = new Sensor(sensor.sensorType(), sensor.numberOfAxis());
        newSensor.setValues(sensor.getValues());
        buffer.set(index, newSensor);

        return this;
    }

    public void commit() {
        sequence.add(buffer);
        resetBuffer();
    }

    private void resetBuffer() {
        List<Sensor> oldBuffer = buffer;
        buffer = new ArrayList<>();

        for (Sensor sensor : oldBuffer) {
            buffer.add(new Sensor(sensor.sensorType(), sensor.numberOfAxis()));
        }
    }

    public int size() {
        return sequence.size();
    }

    public Sensor getLastData(Sensor sensor) {
        int index = sensorOrder.get(sensor);

        List<Sensor> lastData = sequence.get(sequence.size() - 1);
        return lastData.get(index);
    }

    public List<Sensor> getDataByIndex(int index) {
        return sequence.get(index);
    }

    public List<List<Sensor>> getAll() {
        return sequence;
    }

    public List<List<Double>> flatten() {
        List<List<Double>> flattenedSequence = new ArrayList<>();

        for (List<Sensor> sensors : sequence) {
            List<Double> flattenedSensors = flattenSensors(sensors);
            flattenedSequence.add(flattenedSensors);
        }

        return flattenedSequence;
    }

    private List<Double> flattenSensors(List<Sensor> sensors) {
        List<Double> flattenedSensors = new ArrayList<>();

        for (Sensor sensor : sensors) {
            flattenedSensors.addAll(sensor.getValues());
        }

        return flattenedSensors;
    }

    public void clear() {
        sequence.clear();
    }
}
