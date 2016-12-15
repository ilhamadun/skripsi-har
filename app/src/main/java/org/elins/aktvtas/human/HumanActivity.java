package org.elins.aktvtas.human;

import android.content.Context;

import org.elins.aktvtas.R;

import java.util.HashMap;
import java.util.Map;

public class HumanActivity {

    public enum Id {
        STAND(0), SIT(1), WALK(2), RUN(3), WALK_UPSTAIRS(4),
        WALK_DOWNSTAIRS(5), LIE(6), BIKE(7), DRIVE(8), RIDE(9);

        private int ordinal;

        private static Map<Integer, Id> map = new HashMap<>();

        static {
            for (Id id : Id.values()) {
                map.put(id.ordinal, id);
            }
        }

        Id(final int id) {
            ordinal = id;
        }

        public static Id valueOf(int id) {
            return map.get(id);
        }
    }

    private class Resource {
        protected int name;
        protected int icon;

        Resource(int name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private Resource resource;

    public HumanActivity(Id id) {
        switch (id) {
            case STAND:
                resource = new Resource(R.string.stand, R.drawable.ic_stand);
                break;
            case SIT:
                resource = new Resource(R.string.sit, R.drawable.ic_sit);
                break;
            case WALK:
                resource = new Resource(R.string.walk, R.drawable.ic_walk);
                break;
            case RUN:
                resource = new Resource(R.string.run, R.drawable.ic_run);
                break;
            case WALK_UPSTAIRS:
                resource = new Resource(R.string.walking_upstairs, R.drawable.ic_upstairs);
                break;
            case WALK_DOWNSTAIRS:
                resource = new Resource(R.string.walking_downstairs, R.drawable.ic_downstairs);
                break;
            case LIE:
                resource = new Resource(R.string.lie, R.drawable.ic_lie);
                break;
            case BIKE:
                resource = new Resource(R.string.biking, R.drawable.ic_bike);
                break;
            case DRIVE:
                resource = new Resource(R.string.drive, R.drawable.ic_car);
                break;
            case RIDE:
                resource = new Resource(R.string.ride, R.drawable.ic_motorcycle);
                break;
        }
    }

    public int name() {
        return resource.name;
    }

    public String nameString(Context context) {
        return context.getString(resource.name);
    }

    public int icon() {
        return resource.icon;
    }

}
