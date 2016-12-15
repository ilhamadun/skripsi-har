package org.elins.aktvtas;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.elins.aktvtas.human.HumanActivityHistory;
import org.elins.aktvtas.human.HumanActivityHistoryAdapter;

public class PredictionActivity extends AppCompatActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PredictionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView activityHistory = (RecyclerView) findViewById(R.id.human_activity_history);
        activityHistory.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activityHistory.setLayoutManager(layoutManager);

        HumanActivityHistoryAdapter adapter = new HumanActivityHistoryAdapter();
        activityHistory.setAdapter(adapter);
    }
}
