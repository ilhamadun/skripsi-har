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

public class HumanActivityHistoryAdapter
        extends RecyclerView.Adapter<HumanActivityHistoryAdapter.ActivityViewHolder> {

    private List<HumanActivityHistory> histories;

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frame;
        ImageView activityIcon;
        TextView activityName;
        TextView activityInfo;
        ImageView wrongButton;

        ActivityViewHolder(View view) {
            super(view);
            frame = (FrameLayout) view.findViewById(R.id.activity_history_card);
            activityIcon = (ImageView) view.findViewById(R.id.activity_history_icon);
            activityName = (TextView) view.findViewById(R.id.activity_history_name);
            activityInfo = (TextView) view.findViewById(R.id.activity_history_info);
            wrongButton = (ImageView) view.findViewById(R.id.activity_history_wrong_button);
        }
    }

    public HumanActivityHistoryAdapter(int historyLimit) {
        super();
        histories = HumanActivityHistory.getNewest(historyLimit);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.human_activity_history_card, viewGroup, false);

        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ActivityViewHolder activityViewHolder, int i) {
        HumanActivityHistory history = histories.get(i);

        activityViewHolder.activityIcon.setImageResource(history.icon());
        activityViewHolder.activityName.setText(history.name());
        activityViewHolder.activityInfo.setText(history.time());
        activityViewHolder.wrongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = activityViewHolder.getAdapterPosition();
                histories.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    public int addItem(HumanActivityHistory history) {
        histories.add(history);
        notifyItemInserted(histories.size() - 1);

        return histories.size();
    }
}
