package org.elins.aktvtas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.elins.aktvtas.human.HumanActivityHistory;
import org.elins.aktvtas.human.HumanActivityHistoryAdapter;


@Deprecated
public class ActivityHistoryFragment extends Fragment {
    private static final String ACTIVITY_HISTORY_LIMIT = "limit";

    private int historyLimit;
    HumanActivityHistoryAdapter adapter;

    public ActivityHistoryFragment() {
        // Required empty public constructor
    }

    public static ActivityHistoryFragment newInstance(int limit) {
        ActivityHistoryFragment fragment = new ActivityHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ACTIVITY_HISTORY_LIMIT, limit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            historyLimit = getArguments().getInt(ACTIVITY_HISTORY_LIMIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView activityHistory = (RecyclerView) getActivity().findViewById(R.id.human_activity_history);
        activityHistory.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        activityHistory.setLayoutManager(layoutManager);

        adapter = new HumanActivityHistoryAdapter(historyLimit);
        activityHistory.setAdapter(adapter);
    }

    public void addItem(HumanActivityHistory history) {
        adapter.addItem(history);
    }
}
