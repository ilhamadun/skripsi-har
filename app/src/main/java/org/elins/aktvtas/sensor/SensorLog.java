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

    @Column
    public String log_type;

    @Column
    public int number_of_sensors;

    @Column
    public int total_sensor_axis;

    @Column
    public int number_of_entry;

    @Column
    public String log_path;

    @Column
    public String status;

    @Column
    public Date timestamp;

}
