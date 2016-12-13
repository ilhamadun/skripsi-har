package org.elins.aktvtas.human;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.elins.aktvtas.sensor.SensorLog;

import java.util.Date;

@Table(name = "human_activity_history")
public class HumanActivityHistory extends Model {

    @Column
    public String activity_name;

    @Column
    public SensorLog sensor_log;

    @Column
    public int log_row_start;

    @Column
    public int window_size;

    @Column
    public int number_of_window;

    @Column
    public Date start_time;

    @Column
    public Date end_time;

    @Column
    public float confidence;

}
