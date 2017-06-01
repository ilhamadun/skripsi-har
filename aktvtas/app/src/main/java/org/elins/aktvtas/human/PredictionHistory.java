package org.elins.aktvtas.human;

import android.content.Context;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PredictionHistory extends SugarRecord {
    int activity;
    int totalPrediction;
    int correctPrediction;
    float accuracy;
    long timestamp;

    public PredictionHistory() {

    }

    public PredictionHistory(int activity, int totalPrediction, int correctPrediction,
                             float accuracy) {
        this.activity = activity;
        this.totalPrediction = totalPrediction;
        this.correctPrediction = correctPrediction;
        this.accuracy = accuracy;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public String activityName(Context context) {
        HumanActivity humanActivity = new HumanActivity(HumanActivity.Id.valueOf(this.activity));
        return humanActivity.nameString(context);
    }

    public int icon() {
        HumanActivity humanActivity = new HumanActivity(HumanActivity.Id.valueOf(this.activity));
        return humanActivity.icon();
    }

    public int getTotalPrediction() {
        return totalPrediction;
    }

    public int getCorrectPrediction() {
        return correctPrediction;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public static List<PredictionHistory> last(int limit) {
        List<PredictionHistory> histories = PredictionHistory.find(PredictionHistory.class, null,
                null, null, "start_time DESC", Integer.toString(limit));

        return (histories != null) ? histories : new ArrayList<PredictionHistory>();
    }

    public static PredictionHistory last() {
        List<PredictionHistory> histories = PredictionHistory.find(PredictionHistory.class, null,
                null, null, "timestamp DESC", Integer.toString(1));

        return (histories != null) ? histories.get(0) : null;
    }
}
