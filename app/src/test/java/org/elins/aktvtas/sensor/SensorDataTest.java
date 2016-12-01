package org.elins.aktvtas.sensor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SensorDataTest {
    private static final int NUMBER_OF_AXIS = 3;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private SensorData sensorData;
    private Float[] axisValues;
    private List<Float> sensorValues;

    @Before
    public void setupSensorInstance() {
        sensorData = new SensorData(android.hardware.Sensor.TYPE_ACCELEROMETER, NUMBER_OF_AXIS);
    }

    @Before
    public void setupSensorValues() {
        axisValues = new Float[]{-4.26f, 1.25f, 0.126f};
        sensorValues = new ArrayList<>();

        for (Float v : axisValues) {
            sensorValues.add(v);
        }
    }

    @Test
    public void sensorType_isAccelerometer() {
        assertThat(sensorData.sensorType(), is(android.hardware.Sensor.TYPE_ACCELEROMETER));
    }

    @Test
    public void listSize_equals_numberOfAxis() {
        assertThat(sensorData.numberOfAxis(), is(NUMBER_OF_AXIS));
    }

    @Test
    public void setAxisValue_setsCorrectValues() {
        sensorData.setAxisValue(AXIS_X, axisValues[AXIS_X]);
        sensorData.setAxisValue(AXIS_Y, axisValues[AXIS_Y]);
        sensorData.setAxisValue(AXIS_Z, axisValues[AXIS_Z]);

        assertThat(sensorData.getAxisValue(AXIS_X), is(axisValues[AXIS_X]));
        assertThat(sensorData.getAxisValue(AXIS_Y), is(axisValues[AXIS_Y]));
        assertThat(sensorData.getAxisValue(AXIS_Z), is(axisValues[AXIS_Z]));
    }

    @Test
    public void setValues_fromArray_setsCorrectValues() {
        sensorData.setValues(axisValues);
        assertSensorValues();
    }

    @Test
    public void setValues_fromList_setsCorrectValues() {
        sensorData.setValues(sensorValues);
        assertSensorValues();
    }

    private void assertSensorValues() {
        assertThat(sensorData.getValues().size(), is(NUMBER_OF_AXIS));
        assertTrue(sensorData.getValues().equals(sensorValues));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValues_withBiggerNumberOfAxis() {
        Float[] values = {-4.26f, 1.25f};
        sensorData.setValues(values);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValues_withSmallerNumberOfAxis() {
        List<Float> values = new ArrayList<>(sensorValues);
        values.add(6.48f);

        sensorData.setValues(values);
    }
}
