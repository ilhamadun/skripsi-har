package org.elins.aktvtas.human;


import android.content.res.AssetManager;

import org.elins.aktvtas.sensor.SensorDataSequence;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class HumanActivityClassifier {
    private static final String MODEL_FILE = "file:///android_asset/activity_recognition_graph.pb";

    private static final String INPUT_NAME = "input:0";
    private static final String OUTPUT_NAME = "output:0";

    private static final float THRESHOLD = 0.1f;
    private static final int MAX_RESULT = 3;

    private float[] outputs = new float[HumanActivity.Id.values().length];
    private TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface();

    public class Recognition {
        private final int id;
        private final Float confidence;
        private final HumanActivity humanActivity;

        public Recognition(final int id, final Float confidence) {
            this.id = id;
            this.humanActivity = new HumanActivity(HumanActivity.Id.valueOf(id));
            this.confidence = confidence;
        }

        public int getId() {
            return id;
        }

        public int getName() {
            return humanActivity.name();
        }

        public Float getConfidence() {
            return confidence;
        }
    }

    public HumanActivityClassifier(AssetManager assetManager) {
        final int status = inferenceInterface.initializeTensorFlow(assetManager, MODEL_FILE);
        if (status != 0) {
            throw new RuntimeException("TF init status (" + status + ") != 0");
        }
    }

    public List<Recognition> classify(SensorDataSequence sequence) {
        float inputNode[] = sequence.flatten();

        inferenceInterface.fillNodeFloat(INPUT_NAME, new int[] {1, 100, 6, 1}, inputNode);
        inferenceInterface.runInference(new String[] {OUTPUT_NAME});
        inferenceInterface.readNodeFloat(OUTPUT_NAME, outputs);

        return findBestClassification(outputs);
    }

    public List<Recognition> findBestClassification(float[] outputNode) {
        PriorityQueue<Recognition> queue =
            new PriorityQueue<>(
                3,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    }
                 }
            );

        for (int i = 0; i < outputNode.length; i++) {
            if (outputNode[i] > THRESHOLD) {
                queue.add(new Recognition(i, outputNode[i]));
            }
        }

        final List<Recognition> recognitions = new ArrayList<>();
        int recognitionSize = Math.min(queue.size(), MAX_RESULT);

        for (int i = 0; i < recognitionSize; ++i) {
            recognitions.add(queue.poll());
        }

        return recognitions;
    }
}
