package org.elins.aktvtas.network;

import android.content.Context;
import android.widget.Toast;

import org.elins.aktvtas.network.response.RegisterResponse;
import org.elins.aktvtas.network.response.ResponseMessage;
import org.elins.aktvtas.preferences.DeviceIdentifier;
import org.elins.aktvtas.preferences.Preferences;
import org.elins.aktvtas.sensor.SensorLog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {
    private static final String BASE_URL = "http://192.168.33.10:5000/";

    public static final int REGISTER_SUCCESS = 0;
    public static final int NETWORK_ERROR = -1;
    public static final int REGISTER_FAILED = -2;

    private static HARNetworkInterface createService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(HARNetworkInterface.class);
    }

    public static int register(final Context context, String gender, int age,
                                HashMap<String, Boolean> deviceSensors) {
        HARNetworkInterface service = createService();
        Call<RegisterResponse> call = service.register(gender, age,
                deviceSensors.get("accelerometer"),
                deviceSensors.get("ambient_temperature"),
                deviceSensors.get("gravity"),
                deviceSensors.get("gyroscope"),
                deviceSensors.get("light"),
                deviceSensors.get("linear_accelerometer"),
                deviceSensors.get("magnetic_field"),
                deviceSensors.get("orientation"),
                deviceSensors.get("pressure"),
                deviceSensors.get("proximity"),
                deviceSensors.get("relative_humidity"),
                deviceSensors.get("rotation_vector"),
                deviceSensors.get("temperature"));

        int returnCode = REGISTER_FAILED;

        try {
            Response<RegisterResponse> response = call.execute();
            if (response.code() == 201) {
                RegisterResponse message = response.body();
                Preferences.registerDevice(context, message.getDevice(), message.getToken());

                returnCode = REGISTER_SUCCESS;
            }
        } catch (IOException e) {
            returnCode = NETWORK_ERROR;
        }

        return returnCode;
    }


    public static void upload(final Context context, final List<SensorLog> sensorLogs) {
        DeviceIdentifier deviceIdentifier = Preferences.getDeviceIdentifier(context);
        String archivePath = SensorLog.makeArchive(context, sensorLogs);
        final File archive = new File(archivePath);

        RequestBody device = RequestBody.create(MediaType.parse("text/plain"),
                deviceIdentifier.getDevice());
        RequestBody token = RequestBody.create(MediaType.parse("text/plan"),
                deviceIdentifier.getToken());
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), archive);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file",
                archive.getName(), requestFile);

        HARNetworkInterface service = createService();
        Call<ResponseMessage> call = service.upload(device, token, fileBody);

        call.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.code() == 201) {
                    SensorLog.updateStatus(sensorLogs, SensorLog.STATUS_SENT);
                    Toast.makeText(context, "Log files sent to server.", Toast.LENGTH_SHORT).show();
                }
                archive.delete();
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                archive.delete();
                Toast.makeText(context, "Failed to send log files.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
