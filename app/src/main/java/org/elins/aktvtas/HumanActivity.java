package org.elins.aktvtas;

import android.content.Context;

import java.util.Calendar;

public class HumanActivity {
    private String name;
    private int icon;
    private Calendar startTime;
    private Calendar endTime;

    public HumanActivity(Context context, HumanActivityRegister.ActivityId id) {
        HumanActivityRegister register = new HumanActivityRegister(context);
        name = register.name(id);
        icon = register.icon(id);
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }
}
