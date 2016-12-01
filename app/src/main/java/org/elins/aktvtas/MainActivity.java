package org.elins.aktvtas;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawer drawer = setupNavigationDrawer(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment predictionFragment = PredictionFragment.newInstance();
        transaction.add(R.id.content, predictionFragment).commit();
    }

    private Drawer setupNavigationDrawer(Toolbar toolbar) {
        AccountHeader accountHeader = new AccountHeaderBuilder().withActivity(this).build();
        PrimaryDrawerItem prediction = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.prediction);
        PrimaryDrawerItem train = new PrimaryDrawerItem().withIdentifier(2)
                .withName(R.string.train);
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(3)
                .withName(R.string.settings);
        SecondaryDrawerItem help = new SecondaryDrawerItem().withIdentifier(4)
                .withName(R.string.help);
        SecondaryDrawerItem about = new SecondaryDrawerItem().withIdentifier(5)
                .withName(R.string.about);

        return new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        prediction, train,
                        new DividerDrawerItem(),
                        settings, help, about
                )
                .withOnDrawerItemClickListener(drawerItemClickListener())
                .build();

    }

    private Drawer.OnDrawerItemClickListener drawerItemClickListener() {
        return new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (drawerItem.equals(1)) {
                    Fragment predictionFragment = PredictionFragment.newInstance();
                    transaction.replace(R.id.content, predictionFragment).commit();
                } else if (drawerItem.equals(2)) {
                    Fragment trainingChooserFragment = TrainingChooserFragment.newInstance();
                    transaction.replace(R.id.content, trainingChooserFragment).commit();
                } else if (drawerItem.equals(3)) {
                    Snackbar.make(view, R.string.settings, Snackbar.LENGTH_SHORT).show();
                } else if (drawerItem.equals(4)) {
                    Snackbar.make(view, R.string.help, Snackbar.LENGTH_SHORT).show();
                } else if (drawerItem.equals(5)) {
                    Snackbar.make(view, R.string.about, Snackbar.LENGTH_SHORT).show();
                }

                return false;
            }
        };
    }
}
