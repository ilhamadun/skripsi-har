package org.elins.aktvtas.sensor;

import android.content.Context;

import com.orm.SugarRecord;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class SensorLog extends SugarRecord {

    public static String STATUS_PENDING = "PENDING";
    public static String STATUS_SENT = "SENT";

    String logType;
    int numberOfSensors;
    int totalSensorAxis;
    String sensorPosition;
    int numberOfEntry;
    String logPath;
    String status;
    Date timestamp;

    public SensorLog() {
    }

    public SensorLog(String logType, int numberOfSensors, int totalSensorAxis,
                     String sensorPosition, int numberOfEntry, String logPath) {
        this.logType = logType;
        this.numberOfSensors = numberOfSensors;
        this.totalSensorAxis = totalSensorAxis;
        this.sensorPosition = sensorPosition;
        this.numberOfEntry = numberOfEntry;
        this.logPath = logPath;
        this.status = STATUS_PENDING;
        this.timestamp = new Date();
    }

    public SensorDataSequence getLog(int row_start, int window_size, float overlap,
                                     int number_of_windows) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public static SensorLog last() {
        return SensorLog.find(SensorLog.class, null, null, null, "timestamp DESC", "1").get(0);
    }

    public static void updateStatus(List<SensorLog> sensorLogs, String status) {
        for (SensorLog sensorLog : sensorLogs) {
            sensorLog.status = status;
            sensorLog.save();
        }
    }

    public static String makeArchive(Context context, List<SensorLog> sensorLogs) {
        byte[] buffer = new byte[2048];

        String basePath = context.getExternalFilesDir(null).getAbsolutePath();
        String filePath = basePath + "/" + "archive.zip";

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (SensorLog sensorLog : sensorLogs) {
                ZipEntry ze = new ZipEntry(sensorLog.logPath);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(sensorLog.logPath);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

}
