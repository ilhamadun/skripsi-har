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
import android.widget.NumberPicker;


public class TrainingChooserDialogFragment extends DialogFragment {

    NumberPicker trainingTimePicker;
    String[] trainingTimeArray = new String[]{
            "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60"};

    public static TrainingChooserDialogFragment newInstance(int id) {
        TrainingChooserDialogFragment fragment = new TrainingChooserDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_time, null);

        trainingTimePicker = (NumberPicker) view.findViewById(R.id.training_time);
        trainingTimePicker.setMinValue(0);
        trainingTimePicker.setMaxValue(trainingTimeArray.length - 1);
        trainingTimePicker.setDisplayedValues(trainingTimeArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.training_duration)
                .setMessage(R.string.set_training_time)
                .setView(view)
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        startTrainingActivity();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.show();
    }

    public void startTrainingActivity() {
        int activityId = getArguments().getInt("id");
        int trainingDuration = Integer.parseInt(
                trainingTimeArray[trainingTimePicker.getValue()]) * 60;

        Intent intent = new Intent(getActivity(), TrainingActivity.class);
        intent.putExtra(TrainingActivity.ACTIVITY_ID, activityId);
        intent.putExtra(TrainingActivity.TRAINING_DURATION, trainingDuration);
        getActivity().startActivityForResult(intent,
                TrainingActivity.REQUEST_CODE_TRAINING_ACTIVITY);
    }
}
