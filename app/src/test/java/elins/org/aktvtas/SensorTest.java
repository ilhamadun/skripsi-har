package elins.org.aktvtas;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SensorTest {
    private static final int NUMBER_OF_AXIS = 3;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private Sensor sensor;
    private double axisValues[];
    private List<Double> sensorValues;

    @Before
    public void setupSensorInstance() {
        sensor = new Sensor(android.hardware.Sensor.TYPE_ACCELEROMETER, NUMBER_OF_AXIS);
    }

    @Before
    public void setupSensorValues() {
        axisValues = new double[ ]{-4.26d, 1.25d, 0.126d};
        sensorValues = new ArrayList<>();

        for (double v : axisValues) {
            sensorValues.add(v);
        }
    }

    @Test
    public void sensorType_isAccelerometer() {
        assertThat(sensor.sensorType(), is(android.hardware.Sensor.TYPE_ACCELEROMETER));
    }

    @Test
    public void listSize_equals_numberOfAxis() {
        assertThat(sensor.numberOfAxis(), is(NUMBER_OF_AXIS));
    }

    @Test
    public void setAxisValue_setsCorrectValues() {
        sensor.setAxisValue(AXIS_X, axisValues[AXIS_X]);
        sensor.setAxisValue(AXIS_Y, axisValues[AXIS_Y]);
        sensor.setAxisValue(AXIS_Z, axisValues[AXIS_Z]);

        assertThat(sensor.getAxisValue(AXIS_X), is(axisValues[AXIS_X]));
        assertThat(sensor.getAxisValue(AXIS_Y), is(axisValues[AXIS_Y]));
        assertThat(sensor.getAxisValue(AXIS_Z), is(axisValues[AXIS_Z]));
    }

    @Test
    public void setValues_fromArray_setsCorrectValues() {
        sensor.setValues(axisValues);
        assertSensorValues();
    }

    @Test
    public void setValues_fromList_setsCorrectValues() {
        sensor.setValues(sensorValues);
        assertSensorValues();
    }

    private void assertSensorValues() {
        assertThat(sensor.getValues().size(), is(NUMBER_OF_AXIS));
        assertTrue(sensor.getValues().equals(sensorValues));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValues_withBiggerNumberOfAxis() {
        double[] values = {-4.26d, 1.25d};
        sensor.setValues(values);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValues_withSmallerNumberOfAxis() {
        List<Double> values = new ArrayList<>(sensorValues);
        values.add(6.48d);

        sensor.setValues(values);
    }
}
