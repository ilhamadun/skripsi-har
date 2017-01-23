package org.elins.aktvtas.sensor;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.elins.aktvtas.human.HumanActivity;
import org.elins.aktvtas.R;
import org.elins.aktvtas.TrainingActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogSensorService extends SensorService {
    public static final String ACTIVITY_ID = "org.elins.aktvtas.extra.ACTIVITY_ID";
    public static final String SENSOR_POSITION = "org.elins.aktvtas.extra.SENSOR_POSITION";
    public static final String LOG_DURATION_SECOND = "org.elins.aktvtas.extra.LOG_DURATION_SECOND";

    private static final int DEFAULT_LOG_DURATION = 600;
    private  static final int NOTIFICATION_ID = 1;

    String activityName;
    int activityIcon;
    protected CharSequence sensorPosition;
    int logDurationInSecond;


    private final IBinder binder = new LogSensorBinder();
    private NotificationCompat.Builder notificationBuilder;
    private Intent notificationIntent;

    public class LogSensorBinder extends Binder {
        public LogSensorService getService() {
            return LogSensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        HumanActivity.Id activityId = HumanActivity.Id.valueOf(intent.getIntExtra(ACTIVITY_ID, 0));
        HumanActivity humanActivity = new HumanActivity(activityId);
        sensorPosition = intent.getCharSequenceExtra(SENSOR_POSITION);

        logType = "TRAINING_" + String.valueOf(activityId) + "_" + sensorPosition.toString();

        activityName = humanActivity.nameString(this);
        activityIcon = humanActivity.icon();
        logDurationInSecond = intent.getIntExtra(LOG_DURATION_SECOND, DEFAULT_LOG_DURATION);
        int[] sensors = intent.getIntArrayExtra(SENSOR_TO_READ);

        extractSensorToRead(sensors);
        createSensorDataSequence(sensorToRead, numberOfAxis);
        createSensorDataWriter(String.valueOf(activityId));
        createSensorDataReader(sensorToRead);

        foregroundServiceSetup();

        return binder;
    }

    @Override
    public void onDestroy() {
        if (sensorDataWriter != null) {
            sensorDataWriter.close();
            writeToDatabase();
        }
        sensorReader.close();
    }

    protected void writeToDatabase() {
        int totalSensorAxis = 0;
        for (Integer axis : numberOfAxis) {
            totalSensorAxis = totalSensorAxis + axis;
        }

        SensorLog sensorLog = new SensorLog(this.logType, sensorToRead.size(), totalSensorAxis,
                sensorPosition.toString(), entryCounter, filePath);
        sensorLog.save();
    }

    private void foregroundServiceSetup() {
        notificationIntent = new Intent(this, TrainingActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(activityIcon)
                .setContentTitle(activityName)
                .setContentText(getResources().getString(R.string.training_in_progress))
                .setProgress(logDurationInSecond, 0, false)
                .setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void updateNotification(long timeLeftMillis) {
        SimpleDateFormat dateFormat;
        String timeUnit;
        int timeLeftSecond = (int) (timeLeftMillis / 1000);

        if ((timeLeftSecond / 60 > 1)) {
            dateFormat = new SimpleDateFormat("m", Locale.getDefault());
            timeUnit = getResources().getString(R.string.minutes);
        } else {
            dateFormat = new SimpleDateFormat("s", Locale.getDefault());
            timeUnit = getResources().getString(R.string.seconds);
        }

        int timeToGo = logDurationInSecond - timeLeftSecond;
        notificationBuilder.setProgress(logDurationInSecond, timeToGo, false)
                .setContentInfo(dateFormat.format(timeLeftMillis) + " " + timeUnit + " left");
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }
}
