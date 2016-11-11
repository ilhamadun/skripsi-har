package elins.org.aktvtas;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TrainingChooserFragment extends Fragment {

    public TrainingChooserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TrainingChooserFragment.
     */
    public static TrainingChooserFragment newInstance() {
        TrainingChooserFragment fragment = new TrainingChooserFragment();
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

        setOnClickListener(stand, R.string.stand);
        setOnClickListener(sit, R.string.sit);
        setOnClickListener(walk, R.string.walk);
        setOnClickListener(run, R.string.run);
        setOnClickListener(upstairs, R.string.walking_upstairs);
        setOnClickListener(downstairs, R.string.walking_downstairs);
        setOnClickListener(lie, R.string.lie);
        setOnClickListener(bike, R.string.biking);
        setOnClickListener(drive, R.string.drive);
        setOnClickListener(ride, R.string.ride);
    }

    private void setOnClickListener(LinearLayout layout, final int id) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogFragment dialog = TrainingChooserDialogFragment
//                        .newInstance(id);
//                dialog.show(getFragmentManager(), "TrainingChooserDialogFragment");
            }
        });
    }

}
