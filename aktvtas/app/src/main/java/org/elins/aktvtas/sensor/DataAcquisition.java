package org.elins.aktvtas.sensor;


import android.content.Intent;
import android.hardware.Sensor;

import org.elins.aktvtas.human.HumanActivity;

public class DataAcquisition {
    public static final String EXTRA_ACTIVITY_ID = "org.elins.aktvtas.extra.EXTRA_ACTIVITY_ID";
    public static final String EXTRA_SENSOR_PLACEMENT = "org.elins.aktvtas.extra.EXTRA_SENSOR_PLACEMENT";
    public static final String EXTRA_ACQUISITION_DURATION = "org.elins.aktvtas.extra.TRAINING_ACQUISITION";
    public static final int[] SENSOR_TO_READ = {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_LINEAR_ACCELERATION};

    private HumanActivity.Id activityId;
    private int sensorPlacement;
    private int duration;

    public DataAcquisition(Intent intent) {
        activityId = HumanActivity.Id.valueOf(intent.getIntExtra(EXTRA_ACTIVITY_ID, 0));
        sensorPlacement = intent.getIntExtra(EXTRA_SENSOR_PLACEMENT, 0);
        duration = intent.getIntExtra(EXTRA_ACQUISITION_DURATION, 600);
    }

    public HumanActivity.Id getActivityId() {
        return activityId;
    }

    public int getSensorPlacement() {
        return sensorPlacement;
    }

    public int getDuration() {
        return duration;
    }
}
