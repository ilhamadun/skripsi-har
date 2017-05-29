package org.elins.aktvtas.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    public static final String PREFERENCES = "org.example.aktvtas.preferences";
    public static final String DEVICE = "org.example.aktvtas.preferences.device";
    public static final String TOKEN = "org.example.aktvtas.preferences.token";

    public static void registerDevice(Context context, String device, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE, device);
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public static DeviceIdentifier getDeviceIdentifier(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES,
                Context.MODE_PRIVATE);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setDevice(sharedPreferences.getString(DEVICE, ""));
        deviceIdentifier.setToken(sharedPreferences.getString(TOKEN, ""));

        return deviceIdentifier;
    }

    public static boolean deviceIsRegistered(Context context) {
        DeviceIdentifier deviceIdentifier = getDeviceIdentifier(context);

        if (deviceIdentifier.getDevice().equals("") || deviceIdentifier.getToken().equals("")) {
            return false;
        }

        return true;
    }
}
