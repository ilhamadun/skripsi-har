package org.elins.aktvtas.sensor;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class LogReader {
    private static String TAG = "LogReader";
    private CSVReader reader;
    private SensorDataSequence sensorDataSequence;
    private int target;
    private List<Integer> sensorToRead;
    private List<Integer> numberOfAxis;
    private int windowSize;

    public LogReader(Reader reader, List<Integer> sensorToRead,
                     List<Integer> numberOfAxis, int windowSize) {
        this.reader = new CSVReader(reader);
        this.sensorToRead = sensorToRead;
        this.numberOfAxis = numberOfAxis;
        this.windowSize = windowSize;

        sensorDataSequence = SensorDataSequence.create(sensorToRead, numberOfAxis);
    }

    public boolean readNext() {
        boolean status = false;
        try {
            String[] line = reader.readNext();

            if (line != null) {
                sensorDataSequence.clear();
                parse(line);
                status = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    private void parse(String[] line) {
        int lineIndex = 0;
        for (int i = 0; i < windowSize; i++) {
            int sensorIdx = 0;
            for (int sensorType : sensorToRead) {
                SensorData sensorData = new SensorData(sensorType, numberOfAxis.get(sensorIdx));

                float sensorValues[] = new float[numberOfAxis.get(sensorIdx)];
                for (int axis = 0; axis < numberOfAxis.get(sensorIdx); axis++) {
                    sensorValues[axis] = Float.valueOf(line[lineIndex]);
                    lineIndex++;
                }

                sensorData.setValues(sensorValues);
                sensorDataSequence.setData(sensorData);
                sensorIdx++;
            }
            sensorDataSequence.commit();
        }

        target = ((int) Math.floor(Float.valueOf(line[lineIndex])));
    }

    public SensorDataSequence getSensorDataSequence() {
        return sensorDataSequence;
    }

    public int getTarget() {
        return target;
    }
}
