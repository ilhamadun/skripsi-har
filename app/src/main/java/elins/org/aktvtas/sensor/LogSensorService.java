package elins.org.aktvtas.sensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class LogSensorService extends Service {
    public static final String ACTIVITY = "org.elins.aktvtas.extra.ACTIVITY";
    public static final String LOG_DURATION = "org.elins.aktvtas.extra.LOG_DURATION";

    public static final long DEFAULT_LOG_DURATION_IN_SECONDS = 600;

    protected String activity;
    protected long logDurationInSeconds;
    private SensorReader sensorReader;
    private SensorDataWriter sensorDataWriter;

    private final IBinder binder = new LogSensorBinder();

    public class LogSensorBinder extends Binder {
        LogSensorService getService() {
            return LogSensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        activity = intent.getStringExtra(ACTIVITY);
        logDurationInSeconds = intent.getLongExtra(LOG_DURATION, DEFAULT_LOG_DURATION_IN_SECONDS);

        List<Integer> sensorToRead = new ArrayList<>();
        sensorToRead.add(Sensor.TYPE_ACCELEROMETER);
        sensorToRead.add(Sensor.TYPE_GYROSCOPE);
        sensorReader = new SensorReader(this, sensorToRead);

        return binder;
    }

    @Override
    public void onDestroy() {
        if (sensorDataWriter != null) {
            sensorDataWriter.close();
        }
    }

    public void createSensorLog() {
        sensorDataWriter = createSensorDataWriter();
        SensorDataSequence sensorDataSequence = createSensorDataSequence();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < logDurationInSeconds * 1000) {
            List<SensorData> sensorDatas = sensorReader.read();
            if (sensorDatas != null) {
                for (SensorData sensorData : sensorDatas) {
                    sensorDataSequence.setData(sensorData);
                }

                if (sensorDataSequence.size() > 99) {
                    sensorDataWriter.write(sensorDataSequence);
                    sensorDataSequence.clear();
                }
            }
        }

        stopSelf();
    }

    private SensorDataWriter createSensorDataWriter() {
        String basePath = getFilesDir().getAbsolutePath();
        String filePath = basePath + "/" + activity + ".csv";
        return new SensorDataWriter(filePath);
    }

    private SensorDataSequence createSensorDataSequence() {
        while (! sensorReader.readyToRead()) {
            // Wait to read first sensor data
        }
        List<SensorData> sensorDatas = sensorReader.read();
        SensorDataSequence sensorDataSequence = new SensorDataSequence();
        for (SensorData sensorData : sensorDatas) {
            sensorDataSequence.registerSensor(sensorData);
        }

        return sensorDataSequence;
    }
}
