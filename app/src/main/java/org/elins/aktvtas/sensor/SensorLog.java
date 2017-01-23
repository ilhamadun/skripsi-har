package org.elins.aktvtas.sensor;

import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;


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

}
