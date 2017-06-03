package org.elins.aktvtas;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.elins.aktvtas.human.HumanActivity;

public class ActivityChooserFragment extends Fragment {
    public static final int MODE_PREDICTION = 0;
    public static final int MODE_TRAINING = 1;

    private int mode = MODE_PREDICTION;

    public ActivityChooserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ActivityChooserFragment.
     */
    public static ActivityChooserFragment newInstance() {
        ActivityChooserFragment fragment = new ActivityChooserFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_training_chooser, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LinearLayout stand = (LinearLayout) getActivity().findViewById(R.id.training_chooser_stand);
        LinearLayout sit = (LinearLayout) getActivity().findViewById(R.id.training_chooser_sit);
        LinearLayout walk = (LinearLayout) getActivity().findViewById(R.id.training_chooser_walk);
        LinearLayout run = (LinearLayout) getActivity().findViewById(R.id.training_chooser_run);
        LinearLayout upstairs = (LinearLayout) getActivity().findViewById(R.id.training_chooser_upstairs);
        LinearLayout downstairs = (LinearLayout) getActivity().findViewById(R.id.training_chooser_downstairs);
        LinearLayout lie = (LinearLayout) getActivity().findViewById(R.id.training_chooser_lie);
        LinearLayout bike = (LinearLayout) getActivity().findViewById(R.id.training_chooser_bike);
        LinearLayout drive = (LinearLayout) getActivity().findViewById(R.id.training_chooser_drive);
        LinearLayout ride = (LinearLayout) getActivity().findViewById(R.id.training_chooser_ride);

        setOnClickListener(stand, HumanActivity.Id.STAND.ordinal());
        setOnClickListener(sit, HumanActivity.Id.SIT.ordinal());
        setOnClickListener(walk, HumanActivity.Id.WALK.ordinal());
        setOnClickListener(run, HumanActivity.Id.RUN.ordinal());
        setOnClickListener(upstairs, HumanActivity.Id.WALK_UPSTAIRS.ordinal());
        setOnClickListener(downstairs, HumanActivity.Id.WALK_DOWNSTAIRS.ordinal());
        setOnClickListener(lie, HumanActivity.Id.LIE.ordinal());
        setOnClickListener(bike, HumanActivity.Id.BIKE.ordinal());
        setOnClickListener(drive, HumanActivity.Id.DRIVE.ordinal());
        setOnClickListener(ride, HumanActivity.Id.RIDE.ordinal());
    }

    private void setOnClickListener(LinearLayout layout, final int id) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = TrainingChooserDialogFragment
                        .newInstance(mode, id);
                dialog.show(getFragmentManager(), "TrainingChooserDialogFragment");
            }
        });
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
