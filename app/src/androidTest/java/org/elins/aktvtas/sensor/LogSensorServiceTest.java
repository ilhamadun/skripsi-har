package org.elins.aktvtas.sensor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Environment;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.opencsv.CSVReader;

import org.elins.aktvtas.human.HumanActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class LogSensorServiceTest {
    private static LogSensorService service;

    @Mock
    Context mockContext;

    @ClassRule
    public static final ServiceTestRule serviceRule = new ServiceTestRule();

    @Before
    public void bindLogSensorService() throws TimeoutException {
        long logDuration = 10;
        int[] sensorToRead = {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE};

        clearFiles();
        clearDatabase();

        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(),
                LogSensorService.class);

        serviceIntent.putExtra(LogSensorService.ACTIVITY_ID, HumanActivity.Id.STAND);
        serviceIntent.putExtra(LogSensorService.SENSOR_PLACEMENT,
                SensorPlacement.HANDHELD.ordinal());
        serviceIntent.putExtra(LogSensorService.LOG_DURATION_SECOND, logDuration);
        serviceIntent.putExtra(LogSensorService.SENSOR_TO_READ, sensorToRead);

        IBinder binder = serviceRule.bindService(serviceIntent);
        service = ((LogSensorService.LogSensorBinder) binder).getService();
    }

    @After
    public void clearFiles() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/org.elins.aktvtas/files";
        File f = new File(path);
        if (f.isDirectory()) {
            try {
                FileUtils.cleanDirectory(f);
            } catch (IOException e) {

            }
        }
    }

    @After
    public void clearDatabase() {
        SensorLog.deleteAll(SensorLog.class);
    }

    @Test
    public void logFileCreated() throws TimeoutException {
        String expectedPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/org.elins.aktvtas/files/STAND.csv";

        File file = new File(expectedPath);

        assertThat(service.sensorDataWriter.filePath, is(expectedPath));
        assertThat(file.isFile(), is(true));
    }

    @Test
    public void writeSensorDataSequence() throws TimeoutException{
        Random r = new Random();
        Float[] accelerometerData = {r.nextFloat(), r.nextFloat(), r.nextFloat()};
        Float[] gyroscopeData = {r.nextFloat(), r.nextFloat(), r.nextFloat()};

        SensorData accelerometer = new SensorData(Sensor.TYPE_ACCELEROMETER, 3);
        SensorData gyroscope = new SensorData(Sensor.TYPE_GYROSCOPE, 3);

        accelerometer.setValues(accelerometerData);
        gyroscope.setValues(gyroscopeData);

        service.sensorDataSequence.setData(accelerometer);
        service.sensorDataSequence.setData(gyroscope);
        service.sensorDataSequence.commit();

        service.writeLog();

        try {
            File file = new File(service.sensorDataWriter.filePath);
            CSVReader reader = new CSVReader(new FileReader(file));
            List<String[]> rows = reader.readAll();
            reader.close();

            Log.i("LogSensorServiceTest", "File path: " + service.sensorDataWriter.filePath);
            Log.i("LogSensorServiceTest", "File length: " + file.length());

            assertThat(file.length() > 0, is(true));
            assertEquals(rows.size(), 2);

            String[] metadata = rows.get(0);
            assertThat(metadata[SensorDataWriter.METADATA_TYPE],
                    is("TRAINING#" + String.valueOf(HumanActivity.Id.STAND) + "#Handheld"));
            assertThat(Integer.valueOf(metadata[SensorDataWriter.METADATA_NUMBER_OF_SENSORS]),
                    is(2));
            assertThat(Integer.valueOf(metadata[SensorDataWriter.METADATA_NUMBER_OF_ENTRY]),is(1));

            assertThat(rows.get(1).length, is(6));
            assertThat(Float.valueOf(rows.get(1)[0]), is(accelerometerData[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeDatabase() {
        service.writeToDatabase();
        SensorLog last = SensorLog.last();

        assertThat(SensorLog.listAll(SensorLog.class).size(), is(1));
        assertThat(last.logType, is("TRAINING#STAND#HANDHELD"));
        assertThat(last.numberOfSensors, is(2));
        assertThat(last.totalSensorAxis, is(6));
        assertThat(last.sensorPosition, is("HANDHELD"));
        assertThat(last.numberOfEntry, is(1));
        assertThat(last.logPath, is(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/org.elins.aktvtas/files/STAND.csv"));
        assertThat(last.status, is("PENDING"));
    }
}
