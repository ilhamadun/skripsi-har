package org.elins.aktvtas.sensor;

import com.opencsv.CSVWriter;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogWriter {
    protected String filePath;
    protected CSVWriter csvWriter;
    private boolean fileOpened = false;

    public LogWriter(String filePath) {
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
