package org.elins.aktvtas;

import android.support.test.runner.AndroidJUnit4;

import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.sensor.SensorDataSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class HumanActivityClassifierTest {

    @Test
    public void jniClassifierTest() {
        HumanActivityClassifier classifier = new HumanActivityClassifier();
        int activity = classifier.classify(new SensorDataSequence());

        assertThat(activity, is(1));
    }
}
