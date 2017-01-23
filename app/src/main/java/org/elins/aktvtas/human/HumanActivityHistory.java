package org.elins.aktvtas.human;

import android.content.Context;


import com.orm.SugarRecord;

import org.elins.aktvtas.sensor.SensorDataSequence;
import org.elins.aktvtas.sensor.SensorLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HumanActivityHistory extends SugarRecord {

    SensorLog sensorLog;
    int logRowStart;
    int windowSize;
    float overlap;
    int numberOfWindows;
    HumanActivity recognizedActivity;
    HumanActivity actualActivity;
    float confidence;
    Date startTime;
    Date endTime;

    public HumanActivityHistory() {
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

    public static List<HumanActivityHistory> last(int limit) {
        List<HumanActivityHistory> histories = HumanActivityHistory.find(HumanActivityHistory.class,
                null, null, null, "start_time DESC", Integer.toString(limit));

        if (histories != null) {
            return histories;
        } else {
            return new ArrayList<>();
        }
    }

}
