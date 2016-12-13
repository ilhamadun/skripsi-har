package org.elins.aktvtas;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HumanActivityHistoryAdapter
        extends RecyclerView.Adapter<HumanActivityHistoryAdapter.ActivityViewHolder> {

    List<HumanActivity> humanActivities = new ArrayList<>();

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

    @Override
    public int getItemCount() {
        return humanActivities.size();
    }

    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.human_activity_history_card, viewGroup, false);

        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ActivityViewHolder activityViewHolder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        HumanActivity activity = humanActivities.get(i);

        final Date startTime = activity.getStartTime().getTime();
        final Date endTime = activity.getEndTime().getTime();

        activityViewHolder.activityIcon.setImageResource(humanActivities.get(i).getIcon());
        activityViewHolder.activityName.setText(humanActivities.get(i).getName());
        activityViewHolder.activityInfo
                .setText(String.format("%s - %s", dateFormat.format(startTime), dateFormat.format(endTime)));
        activityViewHolder.wrongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = activityViewHolder.getAdapterPosition();
                humanActivities.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    public int addItem(HumanActivity humanActivity) {
        humanActivities.add(humanActivity);
        notifyItemInserted(humanActivities.size() - 1);

        return humanActivities.size();
    }
}
