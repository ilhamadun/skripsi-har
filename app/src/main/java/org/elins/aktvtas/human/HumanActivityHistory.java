package org.elins.aktvtas.human;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.elins.aktvtas.sensor.SensorDataSequence;
import org.elins.aktvtas.sensor.SensorLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public HumanActivity recognizedActivity;

    @Column(name = "actual_activity_id")
    public HumanActivity actualActivity;

    @Column
    public float confidence;

    @Column(name = "start_time")
    public Date startTime;

    @Column(name = "end_time")
    public Date endTime;

    public HumanActivityHistory() {
        super();
    }

    public HumanActivityHistory(SensorLog sensorLog, int logRowStart, int windowSize, float overlap,
                                int numberOfWindows, HumanActivity recognizedActivity,
                                float confidence, Date startTime, Date endTime) {
        this.sensorLog = sensorLog;
        this.logRowStart = logRowStart;
        this.windowSize = windowSize;
        this.overlap = overlap;
        this.numberOfWindows = numberOfWindows;
        this.recognizedActivity = recognizedActivity;
        this.confidence = confidence;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isCorrect() {
        return recognizedActivity.equals(actualActivity);
    }

    public SensorDataSequence getLog() {
        return sensorLog.getLog(logRowStart, windowSize, overlap, numberOfWindows);
    }

    public int name() {
        return actualActivity.name();
    }

    public String nameString(Context context) {
        return actualActivity.nameString(context);
    }

    public int icon() {
        return actualActivity.icon();
    }

    public String time() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        return String.format("%s - %s", dateFormat.format(startTime), dateFormat.format(endTime));
    }

    public static List<HumanActivityHistory> getNewest(int limit) {
        List<HumanActivityHistory> histories = new Select().from(HumanActivityHistory.class)
                .orderBy("start_time DESC")
                .limit(limit)
                .execute();

        if (histories != null) {
            return histories;
        } else {
            return new ArrayList<>();
        }
    }

}
