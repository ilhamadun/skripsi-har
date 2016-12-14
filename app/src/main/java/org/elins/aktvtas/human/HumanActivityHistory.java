package org.elins.aktvtas.human;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.elins.aktvtas.sensor.SensorDataSequence;
import org.elins.aktvtas.sensor.SensorLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Table(name = "human_activity_history")
public class HumanActivityHistory extends Model {

    @Column(name = "sensor_log")
    public SensorLog sensorLog;

    @Column(name = "log_row_start")
    public int logRowStart;

    @Column(name = "window_size")
    public int windowSize;

    @Column
    public float overlap;

    @Column(name = "number_of_windows")
    public int numberOfWindows;

    @Column(name = "recognized_activity_id")
    public HumanActivity recognizedActivityId;

    @Column(name = "actual_activity_id")
    public HumanActivity actualActivityId;

    @Column
    public float confidence;

    @Column(name = "start_time")
    public Date startTime;

    @Column(name = "end_time")
    public Date endTime;

    public boolean isCorrect() {
        return recognizedActivityId.equals(actualActivityId);
    }

    public SensorDataSequence getLog() {
        return sensorLog.getLog(logRowStart, windowSize, overlap, numberOfWindows);
    }

    public int name() {
        return actualActivityId.name();
    }

    public String nameString(Context context) {
        return actualActivityId.nameString(context);
    }

    public int icon() {
        return actualActivityId.icon();
    }

    public String time() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        return String.format("%s - %s", dateFormat.format(startTime), dateFormat.format(endTime));
    }

    public static List<HumanActivityHistory> getNewest(int limit) {
        return new Select().from(HumanActivityHistory.class).orderBy("start_time DESC").limit(limit)
                .execute();
    }

}
