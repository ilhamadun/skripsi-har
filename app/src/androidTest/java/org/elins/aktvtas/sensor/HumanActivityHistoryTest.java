package org.elins.aktvtas.sensor;

import android.support.test.runner.AndroidJUnit4;

import org.elins.aktvtas.human.HumanActivityHistory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class HumanActivityHistoryTest {

    private SensorLog sensorLog;
    private HumanActivityHistory expectedHistory;

    @Before
    public void insertEntry() {
        sensorLog = SensorLogTest.createEntry();

        expectedHistory = new HumanActivityHistory();
        expectedHistory.activity_name = "Standing";
        expectedHistory.sensor_log = sensorLog;
        expectedHistory.log_row_start = 1;
        expectedHistory.window_size = 100;
        expectedHistory.number_of_window = 1000;
        expectedHistory.start_time = new Date();
        expectedHistory.end_time = new Date();
        expectedHistory.confidence = 78.4f;
        expectedHistory.save();
    }

    @Test
    public void loadEntry() {
        HumanActivityHistory history = HumanActivityHistory
                .load(HumanActivityHistory.class, expectedHistory.getId());
        assertThat(history.activity_name, is(expectedHistory.activity_name));
        assertThat(history.sensor_log.total_sensor_axis, is(sensorLog.total_sensor_axis));
        assertThat(history.confidence, is(expectedHistory.confidence));
    }

}
