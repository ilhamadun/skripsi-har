package org.elins.aktvtas.sensor;

import java.util.HashMap;
import java.util.Map;

public enum SensorPlacement {
    RIGHT_PANTS_POCKET(0), LEFT_PANTS_POCKET(1), RIGHT_JACKET_POCKET(2), LEFT_JACKET_POCKET(3),
    HANDHELD(4), BACKPACK(5), SLING_BAG(6);

    private int ordinal;

    private static Map<Integer, SensorPlacement> map = new HashMap<>();

    static {
        for (SensorPlacement position : SensorPlacement.values()) {
            map.put(position.ordinal, position);
        }
    }

    SensorPlacement(final int id) {
        ordinal = id;
    }

    public static SensorPlacement valueOf(int id) {
        return map.get(id);
    }

    public static String toString(int id) {
        SensorPlacement placement = SensorPlacement.valueOf(id);
        String placementString = "UNKNOWN";

        switch (placement) {
            case RIGHT_PANTS_POCKET:
                placementString = "RIGHT_PANTS_POCKET";
                break;
            case LEFT_PANTS_POCKET:
                placementString = "LEFT_PANTS_POCKET";
                break;
            case RIGHT_JACKET_POCKET:
                placementString = "RIGHT_JACKET_POCKET";
                break;
            case LEFT_JACKET_POCKET:
                placementString = "LEFT_JACKET_POCKET";
                break;
            case HANDHELD:
                placementString = "HANDHELD";
                break;
            case BACKPACK:
                placementString = "BACKPACK";
                break;
            case SLING_BAG:
                placementString = "SLING_BAG";
                break;
        }

        return placementString;
    }
}