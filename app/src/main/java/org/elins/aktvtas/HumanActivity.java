package org.elins.aktvtas;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;

public class HumanActivity {
    public static int STAND = 1;
    public static int SIT = 2;
    public static int WALK = 3;
    public static int RUN = 4;
    public static int WALK_UPSTAIRS = 5;
    public static int WALK_DOWNSTAIRS = 6;
    public static int LIE = 7;
    public static int BIKE = 8;
    public static int DRIVE = 9;
    public static int RIDE = 10;

    class Activity {
        protected String name;
        protected int icon;

        public Activity(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private static HashMap<Integer, Activity> activityHashMap = new HashMap<>();

    public HumanActivity(Context context) {
        Resources r = context.getResources();

        activityHashMap.put(STAND, new Activity(r.getString(R.string.stand), R.drawable.ic_stand));
        activityHashMap.put(SIT, new Activity(r.getString(R.string.sit), R.drawable.ic_sit));
        activityHashMap.put(WALK, new Activity(r.getString(R.string.walk), R.drawable.ic_walk));
        activityHashMap.put(RUN, new Activity(r.getString(R.string.run), R.drawable.ic_run));
        activityHashMap.put(WALK_UPSTAIRS,
                new Activity(r.getString(R.string.walking_upstairs), R.drawable.ic_upstairs));
        activityHashMap.put(WALK_DOWNSTAIRS,
                new Activity(r.getString(R.string.walking_downstairs), R.drawable.ic_downstairs));
        activityHashMap.put(LIE, new Activity(r.getString(R.string.lie), R.drawable.ic_lie));
        activityHashMap.put(BIKE, new Activity(r.getString(R.string.biking), R.drawable.ic_bike));
        activityHashMap.put(DRIVE, new Activity(r.getString(R.string.drive), R.drawable.ic_car));
        activityHashMap.put(RIDE,
                new Activity(r.getString(R.string.ride), R.drawable.ic_motorcycle));
    }

    public String name(int id) {
        return activityHashMap.get(id).name;
    }

    public int icon(int id) {
        return activityHashMap.get(id).icon;
    }
}
