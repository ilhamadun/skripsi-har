package org.elins.aktvtas;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.elins.aktvtas.human.PredictionHistory;
import org.elins.aktvtas.human.PredictionHistoryAdapter;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PredictionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PredictionHistoryFragment extends Fragment
        implements PredictionHistoryAdapter.AdapterInterface {
    private static String EXTRA_BASE = "org.elins.aktvtas.extra.";

    public static final int DEFAULT_LIMIT = 10;

    PredictionHistoryAdapter adapter;
    TextView totalAccuracy;
    TextView totalPrediction;
    TextView correctPrediction;

    public PredictionHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PredictionHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PredictionHistoryFragment newInstance() {
        PredictionHistoryFragment fragment = new PredictionHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_prediction_history, container, false);

        totalAccuracy = (TextView) view.findViewById(R.id.home_total_accuracy);
        totalPrediction = (TextView) view.findViewById(R.id.home_total_prediction);
        correctPrediction = (TextView) view.findViewById(R.id.home_correct_prediction);
        RecyclerView predictionHistory = (RecyclerView) view.findViewById(R.id.prediction_history);
        predictionHistory.setHasFixedSize(true);

        updateAccuracy();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        predictionHistory.setLayoutManager(layoutManager);

        adapter = new PredictionHistoryAdapter(this);
        predictionHistory.setAdapter(adapter);

        return view;
    }

    private void updateAccuracy() {
        PredictionHistory.Total total = PredictionHistory.totalPrediction();
        Locale locale = Locale.getDefault();
        totalAccuracy.setText(String.format(locale, "%.2f%%", total.getAccuracy()));
        totalPrediction.setText(String.format(locale, "%d", total.getTotalPrediction()));
        correctPrediction.setText(String.format(locale, "%d", total.getCorrectPrediction()));
    }

    @Override
    public void itemRemoved() {
        updateAccuracy();
    }
}
