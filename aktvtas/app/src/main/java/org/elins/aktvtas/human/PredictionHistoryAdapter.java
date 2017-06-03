package org.elins.aktvtas.human;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.elins.aktvtas.R;

import java.util.List;
import java.util.Locale;

public class PredictionHistoryAdapter extends
        RecyclerView.Adapter<PredictionHistoryAdapter.ActivityViewHolder> {

    private List<PredictionHistory> mHistory;

    public interface AdapterInterface {
        void itemRemoved();
    }

    AdapterInterface buttonListener;

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frame;
        ImageView predictionIcon;
        TextView predictionName;
        TextView predictionAccuracy;
        TextView predictionTime;
        ImageView deleteButton;


        ActivityViewHolder(View view) {
            super(view);
            frame = (FrameLayout) view.findViewById((R.id.prediction_history_card));
            predictionIcon = (ImageView) view.findViewById(R.id.prediction_history_icon);
            predictionName = (TextView) view.findViewById(R.id.prediction_history_name);
            predictionAccuracy = (TextView) view.findViewById(R.id.prediction_history_accuracy);
            predictionTime = (TextView) view.findViewById(R.id.prediction_history_time);
            deleteButton = (ImageView) view.findViewById(R.id.prediction_history_delete_button);
        }
    }

    public PredictionHistoryAdapter(AdapterInterface buttonListener) {
        super();
        mHistory = PredictionHistory.listAll(PredictionHistory.class, "timestamp DESC");
        this.buttonListener = buttonListener;
    }

    @Override
    public int getItemCount() {
        return mHistory.size();
    }

    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.prediction_history_card, viewGroup, false);

        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ActivityViewHolder activityViewHolder, int i) {
        final PredictionHistory history = this.mHistory.get(i);

        String accuracy = String.format(Locale.getDefault(), "%.2f%% (%d/%d)", history.getAccuracy(),
                history.getCorrectPrediction(), history.getTotalPrediction());

        activityViewHolder.predictionIcon.setImageResource(history.icon());
        activityViewHolder.predictionName.setText(history.name());
        activityViewHolder.predictionAccuracy.setText(accuracy);
        activityViewHolder.predictionTime.setText(history.getTime());
        activityViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = activityViewHolder.getAdapterPosition();
                long id = mHistory.get(position).getId();
                PredictionHistory.findById(PredictionHistory.class, id).delete();
                mHistory.remove(position);
                notifyItemRemoved(position);
                buttonListener.itemRemoved();
            }
        });
    }
}
