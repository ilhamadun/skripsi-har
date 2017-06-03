package org.elins.aktvtas.human;

import android.content.Context;
import android.util.Log;

import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PredictionHistory extends SugarRecord {
    private static final String TAG = "PredictionHistory";

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
        this.timestamp = System.currentTimeMillis();
    }

    public int name() {
        HumanActivity humanActivity = new HumanActivity(HumanActivity.Id.valueOf(this.activity));
        return humanActivity.name();
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

    public String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy HH:mm:ss", Locale.getDefault());

        Log.i(TAG, dateFormat.format(timestamp));

        return String.format("%s", dateFormat.format(new Date(timestamp)));
    }

    public static List<PredictionHistory> last(int limit) {
        List<PredictionHistory> histories = PredictionHistory.find(PredictionHistory.class, null,
                null, null, "timestamp DESC", Integer.toString(limit));

        return (histories != null) ? histories : new ArrayList<PredictionHistory>();
    }

    public static PredictionHistory last() {
        List<PredictionHistory> histories = PredictionHistory.find(PredictionHistory.class, null,
                null, null, "timestamp DESC", Integer.toString(1));

        return (histories != null) ? histories.get(0) : null;
    }

    public static class Total {
        private int totalPrediction;
        private int correctPrediction;
        private float accuracy;

        public Total(int totalPrediction, int correctPrediction, float accuracy) {
            this.totalPrediction = totalPrediction;
            this.correctPrediction = correctPrediction;
            this.accuracy = accuracy;
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
    }

    public static Total totalPrediction() {
        List<PredictionHistory> histories = PredictionHistory.listAll(PredictionHistory.class);

        int totalPrediction = 0;
        int correctPrediction = 0;
        float accuracy = 0;

        for (PredictionHistory h : histories) {
            totalPrediction += h.getTotalPrediction();
            correctPrediction += h.getCorrectPrediction();
        }

        if (totalPrediction > 0) {
            accuracy = ((float) correctPrediction / (float) totalPrediction) * 100;
        }

        return new PredictionHistory.Total(totalPrediction, correctPrediction, accuracy);
    }
}
