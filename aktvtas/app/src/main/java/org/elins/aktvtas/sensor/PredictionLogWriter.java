package org.elins.aktvtas.sensor;

public class PredictionLogWriter extends LogWriter {

    public PredictionLogWriter(String filePath) {
        super(filePath);
    }

    public void write(int predictedActivity, float predictionTime) {
        String[] line = {Integer.toString(predictedActivity), Float.toString(predictionTime)};
        csvWriter.writeNext(line, false);
    }

    public void writeMetadata(int activity, int placement, int numberOfSensor) {
        String[] metadata = {
                Integer.toString(activity),
                Integer.toString(placement),
                Integer.toString(numberOfSensor)
        };

        csvWriter.writeNext(metadata, false);
    }
}
