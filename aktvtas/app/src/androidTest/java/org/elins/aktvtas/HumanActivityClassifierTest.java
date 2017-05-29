package org.elins.aktvtas;

import android.content.res.AssetManager;
import android.support.test.runner.AndroidJUnit4;

import org.elins.aktvtas.human.HumanActivityClassifier;
import org.elins.aktvtas.human.Recognition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class HumanActivityClassifierTest {

    @Mock
    AssetManager assetManager;

    @Test
    public void findBestClassification() {
        HumanActivityClassifier classifier = new HumanActivityClassifier(assetManager);

        float[] outputNode = new float[] {0.4f, 0.5f, 0.3f, 0.8f, 0.15f, 0.26f, 0.05f, 0.14f,
                0.26f, 0.75f};

        List<Recognition> result =
                classifier.findBestClassification(outputNode);

        Recognition best = result.get(0);
        assertThat(best.getConfidence(), is(0.8f));
        assertThat(best.getId(), is(3));
    }
}
