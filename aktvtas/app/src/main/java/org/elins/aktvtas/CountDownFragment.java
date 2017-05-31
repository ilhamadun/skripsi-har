package org.elins.aktvtas;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCountDownListener} interface
 * to handle interaction events.
 * Use the {@link CountDownFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountDownFragment extends Fragment {
    private static final String PREPARATION_TIME = "org.elins.aktvtas.extra.PREPARATION_TIME";

    private OnCountDownListener mListener;

    TextView countDownTitle;
    TextView countDownTimeLeft;

    public CountDownFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CountDownFragment.
     */
    public static CountDownFragment newInstance(long preparationTimeInMilliseconds) {
        CountDownFragment fragment = new CountDownFragment();
        Bundle args = new Bundle();
        args.putLong(PREPARATION_TIME, preparationTimeInMilliseconds);
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
        return inflater.inflate(R.layout.fragment_countdown, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        countDownTitle = (TextView) getActivity().findViewById(R.id.countdown_title);
        countDownTimeLeft = (TextView) getActivity().findViewById(R.id.countdown_time_left);

        long preparationTime = getArguments().getLong(PREPARATION_TIME);
        startPreparationCountdown(preparationTime);
    }

    public void changeTitle(String title) {
        countDownTitle.setText(title);
    }

    public void updateTimeLeft(String timeLeft) {
        countDownTimeLeft.setText(timeLeft);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCountDownListener) {
            mListener = (OnCountDownListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCountDownListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCountDownListener {
        // TODO: Update argument type and name
        void onPreparationFinish();
    }

    private void startPreparationCountdown(long preparationTimeInMilliseconds) {
        CountDownTimer countDownTimer = new CountDownTimer(preparationTimeInMilliseconds + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTimeLeft.setText(String.valueOf((millisUntilFinished - 1000) / 1000));
            }

            @Override
            public void onFinish() {
                countDownTitle.setText(R.string.time_left);
                mListener.onPreparationFinish();
            }
        }.start();
    }
}
