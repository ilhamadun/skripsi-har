package org.elins.aktvtas.sensor;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SensorDataSequenceTest {
    private static final int NUMBER_OF_SENSOR = 2;
    private SensorDataSequence sensorDataSequence;
    private SensorData accelerometer;
    private SensorData gyroscope;
    private int numberOfData = 0;

    @Before
    public void initialize() {
        accelerometer = new SensorData(android.hardware.Sensor.TYPE_ACCELEROMETER, 3);
        gyroscope = new SensorData(android.hardware.Sensor.TYPE_GYROSCOPE, 3);

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

        Float firstAccXValue = sensorDataSequence.getDataByIndex(0).get(0).getAxisValue(0);
        Float secondAccXValue = sensorDataSequence.getDataByIndex(1).get(0).getAxisValue(0);

        assertTrue(sensorDataSequence.getLastData(accelerometer)
                .getValues().equals(accelerometer.getValues()));
        assertThat(firstAccXValue, is(not(secondAccXValue)));
    }

    @Test
    public void bufferIsEmptiedAfterCommit() {
        setSensorData();

        assertThat(sensorDataSequence.buffer.get(0).getValues().get(0) == null, is(true));
        assertThat(sensorDataSequence.buffer.get(1).getValues().get(0) == null, is(true));
    }

    @Test
    public void sequenceIsNotAffectedByBufferReset() {
        setSensorData();

        List<Float> accelerometer = sensorDataSequence.getDataByIndex(0).get(0).getValues();

        assertThat(accelerometer.get(0), is(not(0f)));
        assertThat(accelerometer.get(1), is(not(0f)));
        assertThat(accelerometer.get(2), is(not(0f)));
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
        Float[] accelerometerData = {r.nextFloat(), r.nextFloat(), r.nextFloat()};
        Float[] gyroscopeData = {r.nextFloat(), r.nextFloat(), r.nextFloat()};

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

        List<List<Float>> flattenedSequence = sensorDataSequence.flatten();

        assertThat(flattenedSequence.size(), is(sensorDataSequence.size()));
        assertThat(flattenedSequence.get(0).size(), is(6));
    }
}
