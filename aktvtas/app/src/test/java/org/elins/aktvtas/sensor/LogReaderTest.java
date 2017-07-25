package org.elins.aktvtas.sensor;

import android.hardware.Sensor;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LogReaderTest {
    private LogReader logReader;

    @Before
    public void initialize() {
        List<Integer> sensorToRead = new ArrayList<>();
        List<Integer> numberOfAxis = new ArrayList<>();

        sensorToRead.add(Sensor.TYPE_ACCELEROMETER);
        numberOfAxis.add(3);

        sensorToRead.add(Sensor.TYPE_GYROSCOPE);
        numberOfAxis.add(3);

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("sample.csv");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        logReader = new LogReader(inputStreamReader, sensorToRead, numberOfAxis, 100);
    }

    @Test
    public void readFirstLine() {
        logReader.readNext();
        SensorDataSequence sensorDataSequence = logReader.getSensorDataSequence();

        assertThat(sensorDataSequence.size(), is(100));
        assertThat(logReader.getTarget(), is(3));
    }

    @Test
    public void readLastLine() {
        int numberOfSample = 0;
        while (logReader.readNext()) {
            numberOfSample += 1;
        }
        assertThat(numberOfSample, is(3));
        assertThat(logReader.getSensorDataSequence().size(), is(100));
        assertThat(logReader.getTarget(), is(3));
    }
}
