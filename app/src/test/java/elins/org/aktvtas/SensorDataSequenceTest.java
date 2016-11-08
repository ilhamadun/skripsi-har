package elins.org.aktvtas;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SensorDataSequenceTest {
    private static final int NUMBER_OF_SENSOR = 2;
    private SensorDataSequence sensorDataSequence;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private int numberOfData = 0;

    @Before
    public void initialize() {
        accelerometer = new Sensor(android.hardware.Sensor.TYPE_ACCELEROMETER, 3);
        gyroscope = new Sensor(android.hardware.Sensor.TYPE_GYROSCOPE, 3);

        sensorDataSequence = new SensorDataSequence()
                .registerSensor(accelerometer)
                .registerSensor(gyroscope);
    }

    @Test
    public void bufferSize_isCorrect() {
        assertEquals(NUMBER_OF_SENSOR, sensorDataSequence.buffer.size());
    }

    @Test
    public void defaultOrder_isCorrect() {
        assertThat(sensorDataSequence.sensorOrder.get(accelerometer), is(0));
        assertThat(sensorDataSequence.sensorOrder.get(gyroscope), is(1));
    }

    @Test
    public void setSensorData_setsCorrectData() {
        setSensorData();

        assertTrue(sensorDataSequence.getLastData(accelerometer)
                .getValues().equals(accelerometer.getValues()));
    }

    @Test
    public void setALotOfSensorData() {
        for (int i = 0; i < 10; i++) {
            setSensorData();
        }

        assertTrue(sensorDataSequence.getLastData(accelerometer)
                .getValues().equals(accelerometer.getValues()));
    }

    @Test
    public void bufferIsEmptiedAfterCommit() {
        setSensorData();

        List<Double> lastAccelerometer = sensorDataSequence.getLastData(accelerometer).getValues();

        assertThat(lastAccelerometer.get(0), is(0d));
        assertThat(lastAccelerometer.get(1), is(0d));
        assertThat(lastAccelerometer.get(2), is(0d));
    }

    @Test
    public void getAllData() {
        setSensorData();

        assertThat(sensorDataSequence.getAll().size(), is(numberOfData));
    }

    @Test
    public void getDataByIndex() {
        setSensorData();

        assertThat(sensorDataSequence.getDataByIndex(0).size(), is(NUMBER_OF_SENSOR));
    }

    @Test public void clearSequence() {
        setSensorData();

        sensorDataSequence.clear();

        assertThat(sensorDataSequence.getAll().size(), is(0));
    }

    private void setSensorData() {
        Random r = new Random();
        double[] accelerometerData = {r.nextDouble(), r.nextDouble(), r.nextDouble()};
        double[] gyroscopeData = {r.nextDouble(), r.nextDouble(), r.nextDouble()};

        accelerometer.setValues(accelerometerData);
        gyroscope.setValues(gyroscopeData);

        sensorDataSequence.setData(accelerometer).setData(gyroscope).commit();
        numberOfData++;
    }

    @Test
    public void flatten() {
        for (int i = 0; i < 10; i++) {
            setSensorData();
        }

        List<List<Double>> flattenedSequence = sensorDataSequence.flatten();

        assertThat(flattenedSequence.size(), is(sensorDataSequence.size()));
        assertThat(flattenedSequence.get(0).size(), is(6));
    }
}
