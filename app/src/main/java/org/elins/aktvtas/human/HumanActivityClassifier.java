package org.elins.aktvtas.human;


import android.content.res.AssetManager;

import org.elins.aktvtas.sensor.SensorDataSequence;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class HumanActivityClassifier {
    private static final String MODEL_FILE =
            "file:///android_asset/human_activity_recognition_graph.pb";

    private static final String INPUT_NAME = "inputs_placeholder:0";
    private static final String KEEP_PROB = "keep_prob:0";
    private static final String OUTPUT_NAME = "output_node:0";

    private static final float THRESHOLD = 0.1f;
    private static final int MAX_RESULT = 3;

    private float[] outputs = new float[HumanActivity.Id.values().length];
    private TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface();

    public HumanActivityClassifier(AssetManager assetManager) {
        final int status = inferenceInterface.initializeTensorFlow(assetManager, MODEL_FILE);
        if (status != 0) {
            throw new RuntimeException("TF init status (" + status + ") != 0");
        }
    }

    public List<Recognition> classify(SensorDataSequence sequence) {
        float inputNode[] = sequence.flatten();

        inferenceInterface.fillNodeFloat(INPUT_NAME, new int[] {600}, inputNode);
        inferenceInterface.fillNodeFloat(KEEP_PROB, new int[] {1}, new float[] {1.0f});
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
