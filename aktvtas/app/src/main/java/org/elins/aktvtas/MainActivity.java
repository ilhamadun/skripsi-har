package org.elins.aktvtas;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int DRAWER_ID_HOME = 1;
    private static final int DRAWER_ID_PREDICTION = 2;
    private static final int DRAWER_ID_TRAIN = 3;

    private PredictionHistoryFragment predictionHistoryFragment;
    private ActivityChooserFragment activityChooserFragment;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawer drawer = setupNavigationDrawer(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        predictionHistoryFragment = PredictionHistoryFragment.newInstance();
        activityChooserFragment = ActivityChooserFragment.newInstance();

        transaction.add(R.id.content, predictionHistoryFragment).commit();
    }

    private Drawer setupNavigationDrawer(Toolbar toolbar) {
        AccountHeader accountHeader = new AccountHeaderBuilder().withActivity(this).build();
        PrimaryDrawerItem home = new PrimaryDrawerItem().withIdentifier(DRAWER_ID_HOME)
                .withName(R.string.home);
        PrimaryDrawerItem prediction = new PrimaryDrawerItem().withIdentifier(DRAWER_ID_PREDICTION)
                .withName(R.string.prediction);
        PrimaryDrawerItem train = new PrimaryDrawerItem().withIdentifier(DRAWER_ID_TRAIN)
                .withName(R.string.train);

        return new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        home, prediction, train
                )
                .withOnDrawerItemClickListener(drawerItemClickListener())
                .build();

    }

    private Drawer.OnDrawerItemClickListener drawerItemClickListener() {
        return new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (drawerItem.equals(DRAWER_ID_HOME)) {
                    predictionHistoryFragment = PredictionHistoryFragment.newInstance();
                    transaction.replace(R.id.content, predictionHistoryFragment).commit();
                    getSupportActionBar().setTitle(R.string.app_name);
                } else if (drawerItem.equals(DRAWER_ID_PREDICTION)) {
                    transaction.replace(R.id.content, activityChooserFragment).commit();
                    activityChooserFragment.setMode(ActivityChooserFragment.MODE_PREDICTION);
                    getSupportActionBar().setTitle(R.string.prediction);
                } else if (drawerItem.equals(DRAWER_ID_TRAIN)) {
                    transaction.replace(R.id.content, activityChooserFragment).commit();
                    activityChooserFragment.setMode(ActivityChooserFragment.MODE_TRAINING);
                    getSupportActionBar().setTitle(R.string.train);
                }

                return false;
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TrainingActivity.REQUEST_CODE_TRAINING_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                final String filePath = data.getStringExtra(TrainingActivity.RESULT);
                Snackbar.make(findViewById(R.id.content), R.string.training_data_saved,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.delete, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteLogFile(filePath);
                            }
                        })
                        .show();
            }
        }
    }

    public boolean deleteLogFile(String filePath) {
        try {
            File file = new File(filePath);
            Toast.makeText(this, R.string.training_data_deleted, Toast.LENGTH_SHORT).show();
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
