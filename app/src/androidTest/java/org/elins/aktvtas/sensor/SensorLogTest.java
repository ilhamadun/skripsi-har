package org.elins.aktvtas.sensor;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SensorLogTest {

    private SensorLog expectedSensorLog;

    @Before
    public void insertEntry() {
        expectedSensorLog = createEntry();
    }

    public static SensorLog createEntry() {
        SensorLog sensorLog = new SensorLog();
        sensorLog.log_type = SensorLog.LOG_TYPE_PREDICTION;
        sensorLog.number_of_sensors = 1;
        sensorLog.total_sensor_axis = 3;
        sensorLog.number_of_entry = 100;
        sensorLog.log_path = "path";
        sensorLog.status = SensorLog.STATUS_PENDING;
        sensorLog.timestamp = new Date();
        sensorLog.save();

        return sensorLog;
    }

    @After
    public void deleteEntry() {
        expectedSensorLog.delete();
    }

    @Test
    public void loadEntry() {
        SensorLog actualSensorLog = SensorLog.load(SensorLog.class, expectedSensorLog.getId());
        assertThat(actualSensorLog.log_type, is(expectedSensorLog.log_type));
        assertThat(actualSensorLog.total_sensor_axis, is(expectedSensorLog.total_sensor_axis));
        assertThat(actualSensorLog.number_of_entry, is(expectedSensorLog.number_of_entry));
        assertThat(actualSensorLog.log_path, is(expectedSensorLog.log_path));
        assertThat(actualSensorLog.timestamp, is(expectedSensorLog.timestamp));
    }

}
