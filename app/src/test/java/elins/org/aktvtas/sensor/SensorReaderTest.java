package elins.org.aktvtas.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SensorReaderTest {
    private SensorReader sensorReader;
    private Context mockContext = Mockito.mock(Context.class);
    private SensorManager mockSensorManager = Mockito.mock(SensorManager.class);
    private List<Sensor> mockSensor = new ArrayList<>();

    @Test
    public void sensorReader_requestTwo_noneAvailable() {
        List<Integer> sensorToRead = getSensorToRead();

        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(null);
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(null);
        when(mockContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockSensorManager);
        sensorReader = new SensorReader(mockContext, sensorToRead);

        assertThat(sensorReader.getAvailableSensor().size(), is(0));
    }

    @Test
    public void sensorReader_requestTwo_oneAvailable() {
        List<Integer> sensorToRead = getSensorToRead();

        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(mockSensor.get(0));
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(null);
        when(mockContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockSensorManager);
        sensorReader = new SensorReader(mockContext, sensorToRead);

        assertAvailableSensor(1);
    }

    @Test
    public void sensorReader_requestTwo_twoAvailable() {
        setupTwoAvailableSensor();
        assertAvailableSensor(2);
    }

    private List<Integer> getSensorToRead() {
        List<Integer> sensorToRead = new ArrayList<>();
        sensorToRead.add(Sensor.TYPE_ACCELEROMETER);
        sensorToRead.add(Sensor.TYPE_GYROSCOPE);

        for (Integer i : sensorToRead) {
            Sensor sensor = Mockito.mock(Sensor.class);
            when(sensor.getType()).thenReturn(i);
            mockSensor.add(sensor);
        }

        return sensorToRead;
    }

    private void assertAvailableSensor(int expectedNumberOfAvailableSensor) {
        List<Sensor> availableSensor = sensorReader.getAvailableSensor();
        assertThat(availableSensor.size(), is(expectedNumberOfAvailableSensor));
        for (int i = 0; i < availableSensor.size(); i++) {
            assertThat(availableSensor.get(i).getType(), is(mockSensor.get(i).getType()));
        }
    }

    private void setupTwoAvailableSensor() {
        List<Integer> sensorToRead = getSensorToRead();
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(mockSensor.get(0));
        when(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)).thenReturn(mockSensor.get(1));
        when(mockContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockSensorManager);
        sensorReader = new SensorReader(mockContext, sensorToRead);
    }

    @Test
    public void read_whenBufferNotReady() {
        setupTwoAvailableSensor();
        List<SensorData> sensorDatas = sensorReader.read();
        assertEquals(sensorDatas, null);
    }

    @Test
    public void read_whenBufferIsReady() {
        Random r = new Random();
        setupTwoAvailableSensor();
        List<SensorEvent> sensorEvents = new ArrayList<>();

        for (int i = 0; i < sensorReader.getAvailableSensor().size(); i++) {
            float[] values = {r.nextFloat(), r.nextFloat(), r.nextFloat()};

            SensorEvent event = createSensorEvent(values);
            event.sensor = mockSensor.get(i);
            sensorReader.storeAvailableSensorDataToBuffer(event);
            sensorEvents.add(event);
        }

        List<SensorData> sensorDatas = sensorReader.read();

        for (int i = 0; i < sensorDatas.size(); i++) {
            for (int j = 0; j < sensorDatas.size(); j++) {
                assertThat(sensorDatas.get(i).getAxisValue(j), is(sensorEvents.get(i).values[j]));
            }
        }
    }

    private SensorEvent createSensorEvent(float[] values) {
        SensorEvent sensorEvent = Mockito.mock(SensorEvent.class);

        try {
            Field valuesFiled = SensorEvent.class.getField("values");
            valuesFiled.setAccessible(true);

            try {
                valuesFiled.set(sensorEvent, values);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return sensorEvent;
    }
}
