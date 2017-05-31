package org.elins.aktvtas.sensor;


import android.content.Intent;

import org.elins.aktvtas.human.HumanActivity;

public class DataAcquisition {
    public static final String ACTIVITY_ID = "org.elins.aktvtas.extra.ACTIVITY_ID";
    public static final String SENSOR_PLACEMENT = "org.elins.aktvtas.extra.SENSOR_PLACEMENT";
    public static final String ACQUISITION_DURATION = "org.elins.aktvtas.extra.TRAINING_ACQUISITION";

    private HumanActivity.Id activityId;
    private int sensorPlacement;
    private int duration;

    public DataAcquisition(Intent intent) {
        activityId = HumanActivity.Id.valueOf(intent.getIntExtra(ACTIVITY_ID, 0));
        sensorPlacement = intent.getIntExtra(SENSOR_PLACEMENT, 0);
        duration = intent.getIntExtra(ACQUISITION_DURATION, 600);
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
