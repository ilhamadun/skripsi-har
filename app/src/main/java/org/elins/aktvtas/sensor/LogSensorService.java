package org.elins.aktvtas.sensor;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.elins.aktvtas.HumanActivity;
import org.elins.aktvtas.R;
import org.elins.aktvtas.TrainingActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogSensorService extends Service implements SensorReader.SensorReaderEvent {
    public static final String ACTIVITY_ID = "org.elins.aktvtas.extra.ACTIVITY_ID";
    public static final String LOG_DURATION_SECOND = "org.elins.aktvtas.extra.LOG_DURATION_SECOND";
    public static final String SENSOR_TO_READ = "org.elins.aktvtas.extra.SENSOR_TO_READ";

    private static final int DEFAULT_LOG_DURATION = 600;
    private  static final int NOTIFICATION_ID = 1;

    int activityId;
    String activityName;
    int activityIcon;
    int logDurationInSecond;
    private SensorReader sensorReader;
    private List<SensorData> buffer;
    protected SensorDataSequence sensorDataSequence;
    protected SensorDataWriter sensorDataWriter;

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
        activityId = intent.getIntExtra(ACTIVITY_ID, 0);
        HumanActivity humanActivity = new HumanActivity(this);
        activityName = humanActivity.name(activityId);
        activityIcon = humanActivity.icon(activityId);
        logDurationInSecond = intent.getIntExtra(LOG_DURATION_SECOND, DEFAULT_LOG_DURATION);
        int[] sensors = intent.getIntArrayExtra(SENSOR_TO_READ);

        List<Integer> sensorToRead = new ArrayList<>();
        for (int sensor : sensors) {
            sensorToRead.add(sensor);
        }
        createSensorDataSequence(sensorToRead);
        sensorDataWriter = createSensorDataWriter();
        sensorReader = new SensorReader(this, sensorToRead);
        sensorReader.enableEventCallback(this);

        foregroundServiceSetup();

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        if (sensorDataWriter != null) {
            sensorDataWriter.close();
        }
        sensorReader.close();
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

    public List<SensorData> getLastSensorData() {
        if (sensorDataSequence.size() > 0) {
            return sensorDataSequence.getLastData();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onSensorDataReady() {
        buffer = sensorReader.read();
        if (buffer != null) {
            for (SensorData data : buffer) {
                sensorDataSequence.setData(data);
            }
            sensorDataSequence.commit();

            if (sensorDataSequence.size() % 1500 == 0) {
                writeLog();
                sensorDataSequence.clear();
            }
        }
    }

    protected SensorDataWriter createSensorDataWriter() {
        String basePath = getExternalFilesDir(null).getAbsolutePath();
        String filePath = basePath + "/" + activityName + ".csv";
        return new SensorDataWriter(filePath);
    }

    protected void createSensorDataSequence(List<Integer> sensorToRead) {
        sensorDataSequence = new SensorDataSequence();
        for (int sensor : sensorToRead) {
            SensorData sensorData = new SensorData(sensor, 3);
            // TODO: 12/3/2016 Number of axis as extra
            sensorDataSequence.registerSensor(sensorData);
        }
    }

    protected void writeLog() {
        sensorDataWriter.open();
        sensorDataWriter.write(sensorDataSequence);
        sensorDataWriter.close();
    }
}
