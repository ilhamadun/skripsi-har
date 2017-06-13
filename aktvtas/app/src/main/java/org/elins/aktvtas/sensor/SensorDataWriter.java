package org.elins.aktvtas.sensor;

import com.opencsv.CSVWriter;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorDataWriter extends LogWriter {
    public static final int METADATA_TYPE = 0;
    public static final int METADATA_NUMBER_OF_SENSORS = 1;
    public static final int METADATA_TOTAL_SENSOR_AXIS = 2;
    public static final int METADATA_NUMBER_OF_ENTRY = 3;

    private long entryCounter = 0;

    public SensorDataWriter(String filePath) {
        super(filePath);
    }

    public void write(SensorDataSequence sequence) {
        List<String[]> sequenceStrings = convertToListOfString(sequence);
        csvWriter.writeAll(sequenceStrings);

        entryCounter += sequenceStrings.size();
    }

    public void write(String type, int numberOfSensor, int totalSensorAxis,
                      SensorDataSequence sequence) {
        if (sequence.size() > 0) {
            List<String[]> sequenceStrings = convertToListOfString(sequence);

            // Trim latest 10 seconds data
            if (sequenceStrings.size() > 500) {
                sequenceStrings = sequenceStrings.subList(0, sequenceStrings.size() - 500);
                entryCounter = entryCounter - 500;
            }

            writeMetadata(type, numberOfSensor, totalSensorAxis, sequenceStrings.size());
            csvWriter.writeAll(sequenceStrings, false);

            entryCounter += sequenceStrings.size();
        }
    }

    private void writeMetadata(String type, int numberOfSensor, int totalSensorAxis,
                               int numberOfEntry) {
        String[] metadata = new String[4];
        metadata[METADATA_TYPE] = type;
        metadata[METADATA_NUMBER_OF_SENSORS] = String.valueOf(numberOfSensor);
        metadata[METADATA_TOTAL_SENSOR_AXIS] = String.valueOf(totalSensorAxis);
        metadata[METADATA_NUMBER_OF_ENTRY] = String.valueOf(numberOfEntry);

        csvWriter.writeNext(metadata, false);
    }

    protected List<String[]> convertToListOfString(SensorDataSequence sequence) {
        List<String[]> sequenceString = new ArrayList<>();
        List<List<Float>> flattenedSequence = sequence.matrix();

        for (List<Float> sensorValue : flattenedSequence) {
            String[] buffer = new String[sensorValue.size()];
            for (int i = 0; i < sensorValue.size(); i++) {
                buffer[i] = sensorValue.get(i).toString();
            }
            sequenceString.add(buffer);
        }

        return sequenceString;
    }
}
