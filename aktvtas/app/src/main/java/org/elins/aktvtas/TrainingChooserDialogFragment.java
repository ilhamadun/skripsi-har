package org.elins.aktvtas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import org.apache.commons.lang3.NotImplementedException;

public class TrainingChooserDialogFragment extends DialogFragment implements
        AdapterView.OnItemSelectedListener {

    NumberPicker timePicker;
    String[] timeArray = new String[]{
            "1", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60"};

    Spinner positionSpinner;
    int sensorPlacement;
    int mode;

    public static TrainingChooserDialogFragment newInstance(int mode, int id) {
        TrainingChooserDialogFragment fragment = new TrainingChooserDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        fragment.setArguments(args);
        fragment.mode = mode;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_time, null);

        timePicker = (NumberPicker) view.findViewById(R.id.training_time);
        timePicker.setMinValue(0);
        timePicker.setMaxValue(timeArray.length - 1);
        timePicker.setDisplayedValues(timeArray);

        positionSpinner = (Spinner) view.findViewById(R.id.training_position);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.training_position_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);
        positionSpinner.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(dialogTitle(mode))
                .setView(view)
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        if (mode == ActivityChooserFragment.MODE_PREDICTION) {
                            startPredictionActivity();
                        } else {
                            startTrainingActivity();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.show();
    }

    private int dialogTitle(int mode) {
        int title;
        if (mode == ActivityChooserFragment.MODE_PREDICTION) {
            title = R.string.prediction_options;
        } else {
            title = R.string.training_options;
        }

        return title;
    }

    public void startPredictionActivity() {
        int activityId = getArguments().getInt("id");
        int trainingDuration = Integer.parseInt(
                timeArray[timePicker.getValue()]) * 60;

        PredictionActivity.startActivity(getActivity(), activityId, sensorPlacement,
                trainingDuration);
    }

    public void startTrainingActivity() {
        int activityId = getArguments().getInt("id");
        int trainingDuration = Integer.parseInt(
                timeArray[timePicker.getValue()]) * 60;

        Intent intent = new Intent(getActivity(), TrainingActivity.class);
        intent.putExtra(TrainingActivity.ACTIVITY_ID, activityId);
        intent.putExtra(TrainingActivity.SENSOR_PLACEMENT, sensorPlacement);
        intent.putExtra(TrainingActivity.TRAINING_DURATION, trainingDuration);
        getActivity().startActivityForResult(intent,
                TrainingActivity.REQUEST_CODE_TRAINING_ACTIVITY);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        sensorPlacement = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        sensorPlacement = 0;
    }
}
