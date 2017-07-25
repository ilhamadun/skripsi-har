package org.elins.aktvtas;

import android.hardware.Sensor;
import android.support.test.runner.AndroidJUnit4;

import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.human.Recognition;
import org.elins.aktvtas.sensor.LogReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class HumanActivityClassifierTest {
    private HumanActivityClassifier classifier;
    private LogReader logReader;

    @Before
    public void initialize() {
        classifier = new HumanActivityClassifier(getInstrumentation().getTargetContext()
                .getResources().getAssets());

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
    public void classification() {
        int correctPrediction = 0;
        int totalPrediction = 0;
        while (logReader.readNext()) {
            List<Recognition> recognitions = classifier.classify(logReader.getSensorDataSequence());

            if (recognitions.get(0).getId() == logReader.getTarget()) {
                correctPrediction++;
            }
            totalPrediction++;
        }
        float accuracy = correctPrediction / (float) totalPrediction;

        assertThat(accuracy > 0.8f, is(true));
    }
}
