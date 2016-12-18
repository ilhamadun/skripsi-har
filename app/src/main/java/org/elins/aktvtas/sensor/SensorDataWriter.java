package org.elins.aktvtas.sensor;

import com.opencsv.CSVWriter;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorDataWriter {
    public static final int METADATA_TYPE = 0;
    public static final int METADATA_NUMBER_OF_SENSORS = 1;
    public static final int METADATA_TOTAL_SENSOR_AXIS = 2;
    public static final int METADATA_NUMBER_OF_ENTRY = 3;

    protected String filePath;
    private CSVWriter csvWriter;
    private boolean fileOpened = false;
    private long entryCounter = 0;

    public SensorDataWriter(String filePath) {
        filePath = confirmFileExtensionIsCsv(filePath);
        initializeWriter(filePath);
    }

    protected String confirmFileExtensionIsCsv(String filePath) {
        String extension = filePath.substring(filePath.length() - 4);

        if (! extension.equals(".csv")) {
            filePath = filePath + ".csv";
        }

        return filePath;
    }

    private void initializeWriter(String filePath) {
        File file = new File(filePath);

        while (file.exists()) {
            filePath = createUniquePath(filePath);
            file = new File(filePath);
        }

        this.filePath = filePath;
        createWriter(filePath);
    }

    protected String createUniquePath(String filePath) {
        String newPath = stripFileExtension(filePath);
        newPath = appendUniqueNumber(newPath);

        return newPath + ".csv";

    }

    private String stripFileExtension(String filePath) {
        return filePath.substring(0, filePath.length() - 4);
    }

    private String appendUniqueNumber(String filePath) {
        int numberOfDigits = numberOfDigitsInUniqueNumber(filePath);
        int fileUniqueNumber = getEmbeddedUniqueNumber(filePath, numberOfDigits);
        fileUniqueNumber++;

        filePath = stripPreviousUniqueNumber(filePath, numberOfDigits);

        return filePath + String.valueOf(fileUniqueNumber);
    }

    private int numberOfDigitsInUniqueNumber(String filePath) {
        int numberOfDigits = 0;

        String embeddedUniqueNumber = getStringOfEmbeddedUniqueNumber(filePath, numberOfDigits + 1);

        while (StringUtils.isNumeric(embeddedUniqueNumber)) {
            numberOfDigits++;
            embeddedUniqueNumber = getStringOfEmbeddedUniqueNumber(filePath, numberOfDigits + 1);
        }

        return numberOfDigits;
    }

    private String getStringOfEmbeddedUniqueNumber(String filePath, int numberOfDigits) {
        return filePath.substring(filePath.length() - numberOfDigits);
    }

    private int getEmbeddedUniqueNumber(String filePath, int numberOfDigits) {
        if (numberOfDigits > 0) {
            String embeddedUniqueNumber = filePath.substring(filePath.length() - numberOfDigits);
            return Integer.valueOf(embeddedUniqueNumber);
        } else {
            return 0;
        }
    }

    private String stripPreviousUniqueNumber(String filePath, int numberOfDigits) {
        return filePath.substring(0, filePath.length() - numberOfDigits);
    }

    private void createWriter(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            csvWriter = new CSVWriter(fileWriter);
            fileOpened = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        List<List<Float>> flattenedSequence = sequence.flatten();

        for (List<Float> sensorValue : flattenedSequence) {
            String[] buffer = new String[sensorValue.size()];
            for (int i = 0; i < sensorValue.size(); i++) {
                buffer[i] = sensorValue.get(i).toString();
            }
            sequenceString.add(buffer);
        }

        return sequenceString;
    }

    public void open() {
        if (! fileOpened) {
            createWriter(filePath);
        }
    }

    public void close() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
                fileOpened = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        try {
            File file = new File(filePath);

            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
