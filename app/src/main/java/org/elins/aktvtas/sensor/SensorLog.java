package org.elins.aktvtas.sensor;

import android.hardware.Sensor;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;


@Table(name = "sensor_log")
public class SensorLog extends Model {

    public static String LOG_TYPE_TRAINING = "TRAINING";
    public static String LOG_TYPE_PREDICTION = "PREDICTION";

    public static String STATUS_PENDING = "PENDING";
    public static String STATUS_SENT = "SENT";

    @Column(name = "log_type")
    public String logType;

    @Column(name = "number_of_sensors")
    public int numberOfSensors;

    @Column(name = "total_sensor_axis")
    public int totalSensorAxis;

    @Column(name = "number_of_entry")
    public int numberOfEntry;

    @Column(name = "log_path")
    public String logPath;

    @Column
    public String status;

    @Column
    public Date timestamp;

    public SensorLog() {
        super();
    }

    public SensorLog(String logType, int numberOfSensors, int totalSensorAxis, int numberOfEntry,
                     String logPath) {
        this.logType = logType;
        this.numberOfSensors = numberOfSensors;
        this.totalSensorAxis = totalSensorAxis;
        this.numberOfEntry = numberOfEntry;
        this.logPath = logPath;
    }

    public SensorDataSequence getLog(int row_start, int window_size, float overlap,
                                     int number_of_windows) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
