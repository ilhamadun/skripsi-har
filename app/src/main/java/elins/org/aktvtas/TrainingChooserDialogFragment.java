package elins.org.aktvtas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;


public class TrainingChooserDialogFragment extends DialogFragment {

    NumberPicker trainingTimePicker;
    String[] trainingTimeArray = new String[]{
            "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60"};

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
                .setMessage("Set training time in minutes:")
                .setView(view)
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        TrainingFragment trainer = TrainingFragment.newInstance(
                                getArguments().getInt("id"),
                                Integer.parseInt(trainingTimeArray[trainingTimePicker.getValue()]));
                        transaction.replace(R.id.content, trainer).commit();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.show();
    }

}
