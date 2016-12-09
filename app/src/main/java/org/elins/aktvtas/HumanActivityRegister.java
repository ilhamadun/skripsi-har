package org.elins.aktvtas;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;

public class HumanActivityRegister {
    public enum ActivityId {STAND, SIT, WALK, RUN, WALK_UPSTAIRS, WALK_DOWNSTAIRS, LIE, BIKE, DRIVE, RIDE}

    private class Activity {
        protected String name;
        protected int icon;

        public Activity(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private static HashMap<ActivityId, Activity> activityHashMap = new HashMap<>();

    public HumanActivityRegister(Context context) {
        Resources r = context.getResources();

        activityHashMap.put(ActivityId.STAND, new Activity(r.getString(R.string.stand), R.drawable.ic_stand));
        activityHashMap.put(ActivityId.SIT, new Activity(r.getString(R.string.sit), R.drawable.ic_sit));
        activityHashMap.put(ActivityId.WALK, new Activity(r.getString(R.string.walk), R.drawable.ic_walk));
        activityHashMap.put(ActivityId.RUN, new Activity(r.getString(R.string.run), R.drawable.ic_run));
        activityHashMap.put(ActivityId.WALK_UPSTAIRS,
                new Activity(r.getString(R.string.walking_upstairs), R.drawable.ic_upstairs));
        activityHashMap.put(ActivityId.WALK_DOWNSTAIRS,
                new Activity(r.getString(R.string.walking_downstairs), R.drawable.ic_downstairs));
        activityHashMap.put(ActivityId.LIE, new Activity(r.getString(R.string.lie), R.drawable.ic_lie));
        activityHashMap.put(ActivityId.BIKE, new Activity(r.getString(R.string.biking), R.drawable.ic_bike));
        activityHashMap.put(ActivityId.DRIVE, new Activity(r.getString(R.string.drive), R.drawable.ic_car));
        activityHashMap.put(ActivityId.RIDE,
                new Activity(r.getString(R.string.ride), R.drawable.ic_motorcycle));
    }

    public String name(ActivityId id) {
        return activityHashMap.get(id).name;
    }

    public int icon(ActivityId id) {
        return activityHashMap.get(id).icon;
    }
}
