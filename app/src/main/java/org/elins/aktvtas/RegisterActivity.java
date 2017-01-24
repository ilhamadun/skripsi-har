package org.elins.aktvtas;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.elins.aktvtas.network.NetworkManager;
import org.elins.aktvtas.sensor.SensorReader;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    TextView age;
    Spinner genderSpinner;
    Button continueButton;

    String gender;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        age = (TextView) findViewById(R.id.age);

        genderSpinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        continueButton = (Button) findViewById(R.id.register_button);

        age.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (validateInput()) {
                        new RegisterDeviceTask().execute();
                    }
                    handled = true;
                }

                return handled;
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    new RegisterDeviceTask().execute();
                }
            }
        });
    }

    private boolean validateInput() {
        if (age.getText().length() == 0) {
            Toast.makeText(this, getString(R.string.you_need_to_fill_your_age), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private class RegisterDeviceTask extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... params) {
            return registerDevice();
        }

        protected  void onPostExecute(Integer status) {
            if (status == NetworkManager.REGISTER_SUCCESS) {
                MainActivity.startActivity(getApplicationContext());
            } else if (status == NetworkManager.NETWORK_ERROR) {
                Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_register_device),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int registerDevice() {
        HashMap<String, Boolean> sensors = SensorReader.listAllAvailableSensor(this);
        return NetworkManager.register(this, gender, Integer.valueOf(age.getText().toString()),
                sensors);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (pos == 0) {
            gender = "Male";
        } else if (pos == 1) {
            gender = "Female";
        } else {
            gender = "Unspecified";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        gender = "Unspecified";
    }
}
