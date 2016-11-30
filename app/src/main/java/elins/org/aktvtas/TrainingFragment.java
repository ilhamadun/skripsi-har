package elins.org.aktvtas;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrainingFragment extends Fragment {
    private static final String ACTIVITY_ID = "activity_id";
    private static final String TRAINING_DURATION = "training_duration";

    private int activityId;
    private int trainingDuration;

    private CountDownTimer countDownTimer;

    private TextView activityName;
    private TextView timeLeftText;
    private TextView timerContainer;
    private Button controlButton;
    private LinearLayout sensorMonitor;

    List<View> sensorMonitorLayouts = new ArrayList<>();


    public TrainingFragment() {
        // Required empty public constructor
    }

    public static TrainingFragment newInstance(int activityId, int trainingDuration) {
        TrainingFragment fragment = new TrainingFragment();
        Bundle args = new Bundle();
        args.putInt(ACTIVITY_ID, activityId);
        args.putInt(TRAINING_DURATION, trainingDuration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            activityId = getArguments().getInt(ACTIVITY_ID);
            trainingDuration = getArguments().getInt(TRAINING_DURATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        registerViewComponents();
        activityName.setText(activityId);
        startPreparationCountdown(10000);

    }

    private void registerViewComponents() {
        activityName = (TextView) getActivity().findViewById(R.id.trainer_activity_name);
        timeLeftText = (TextView) getActivity().findViewById(R.id.trainer_time_left);
        timerContainer = (TextView) getActivity().findViewById(R.id.trainer_duration);
        sensorMonitor = (LinearLayout) getActivity().findViewById(R.id.sensor_monitor);
        controlButton = (Button) getActivity().findViewById(R.id.trainer_control_button);

        addSensorMonitorLayout("Accelerometer");
        addSensorMonitorLayout("Gyroscope");

        controlButton = (Button) getActivity().findViewById(R.id.trainer_control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
            }
        });
    }

    private int addSensorMonitorLayout(String name) {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.sensor_monitor,
                sensorMonitor, false);

        sensorMonitor.addView(layout);
        TextView title = (TextView) layout.findViewById(R.id.sensor_monitor_title);
        title.setText(name);

        sensorMonitorLayouts.add(layout);

        return sensorMonitorLayouts.size() - 1;
    }

    private void startPreparationCountdown(long preparationTimeInMilliseconds) {
        countDownTimer = new CountDownTimer(preparationTimeInMilliseconds + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerContainer.setText(String.valueOf((millisUntilFinished - 1000) / 1000));
            }

            @Override
            public void onFinish() {
                timeLeftText.setText(R.string.time_left);
                startTrainingCountdown();
            }
        }.start();
    }

    private void startTrainingCountdown() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        countDownTimer = new CountDownTimer((trainingDuration * 60 * 1000) + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished - 1000);
                timerContainer.setText(dateFormat.format(date));
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

}
