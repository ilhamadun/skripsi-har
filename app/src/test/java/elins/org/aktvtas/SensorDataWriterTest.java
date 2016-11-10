package elins.org.aktvtas;

import com.opencsv.CSVReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SensorDataWriterTest {

    public static final String FILE_NAME = "activity_stand.csv";
    public static final int NUMBER_OF_SEQUENCE = 100;

    private SensorData accelerometer = new SensorData(android.hardware.Sensor.TYPE_ACCELEROMETER, 3);
    private SensorData gyroscope = new SensorData(android.hardware.Sensor.TYPE_GYROSCOPE, 3);
    private SensorDataSequence sensorDataSequence;
    private static SensorDataWriter sensorDataWriter;

    @Before
    public void initializeSensorDataSequence() {
        sensorDataSequence = new SensorDataSequence()
                .registerSensor(accelerometer)
                .registerSensor(gyroscope);

        for (int i = 0; i < NUMBER_OF_SEQUENCE; i++)
        {
            Random r = new Random();
            double[] accelerometerData = {r.nextDouble(), r.nextDouble(), r.nextDouble()};
            double[] gyroscopeData = {r.nextDouble(), r.nextDouble(), r.nextDouble()};

            accelerometer.setValues(accelerometerData);
            gyroscope.setValues(gyroscopeData);

            sensorDataSequence.setData(accelerometer).setData(gyroscope).commit();
        }
    }

    @BeforeClass
    public static void initializeSensorDataWriter() {
        sensorDataWriter = new SensorDataWriter(FILE_NAME);
    }

    @Test
    public void confirmFileExtensionIsCsv() {
        String filePath = "activity.cvs";
        filePath = sensorDataWriter.confirmFileExtensionIsCsv(filePath);

        assertThat(filePath, is("activity.cvs.csv"));
    }

    @Test
    public void createUniquePath_fromPathWithNoEmbeddedNumber() {
        String filePath = "activity.csv";
        String newPath = sensorDataWriter.createUniquePath(filePath);

        assertThat(newPath, is("activity1.csv"));
    }

    @Test
    public void createUniquePath_fromPathWithEmbeddedNumber() {
        String filePath = "activity1.csv";
        String newPath = sensorDataWriter.createUniquePath(filePath);

        assertThat(newPath, is("activity2.csv"));
    }

    @Test
    public void convertToListOfString() {
        List<String[]> sequenceStrings = sensorDataWriter.convertToListOfString(sensorDataSequence);

        assertThat(sequenceStrings.size(), is(sensorDataSequence.size()));
        assertThat(sequenceStrings.get(0).length, is(6));
        assertThat(sequenceStrings.get(0)[0],
                is(String.valueOf(sensorDataSequence.getDataByIndex(0).get(0).getAxisValue(0))));
    }

    @Test
    public void write() {
        try {
            sensorDataWriter.write(sensorDataSequence);
            sensorDataWriter.close();

            FileReader fileReader = new FileReader(sensorDataWriter.filePath);
            CSVReader reader = new CSVReader(fileReader);
            List<String[]> rows = reader.readAll();
            reader.close();

            assertThat(Double.valueOf(rows.get(5)[2]),
                    is(sensorDataSequence.getDataByIndex(5).get(0).getAxisValue(2)));

            sensorDataWriter.delete();
        } catch (FileNotFoundException e) {
            assertTrue(false);
        } catch (IOException e) {
            assertFalse(true);
        }
    }

}
