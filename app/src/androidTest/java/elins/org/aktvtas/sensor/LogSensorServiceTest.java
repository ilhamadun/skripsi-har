package elins.org.aktvtas.sensor;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class LogSensorServiceTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void boundLogService() throws TimeoutException {
        long logDuration = 10;
        String activity = "stand";
        LogSensorService service = startLogSensor(activity, logDuration);

        assertThat(service.logDurationInSeconds, is(logDuration));
        assertThat(service.activity, is(activity));
    }

    private LogSensorService startLogSensor(String activity, long logDuration)
            throws TimeoutException {
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(),
                LogSensorService.class);

        serviceIntent.putExtra(LogSensorService.ACTIVITY, activity);
        serviceIntent.putExtra(LogSensorService.LOG_DURATION, logDuration);

        IBinder binder = serviceRule.bindService(serviceIntent);
        return ((LogSensorService.LogSensorBinder) binder).getService();
    }
}
