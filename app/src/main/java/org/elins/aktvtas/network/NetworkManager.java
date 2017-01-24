package org.elins.aktvtas.network;

import android.content.Context;

import org.elins.aktvtas.network.response.RegisterResponse;
import org.elins.aktvtas.network.response.ResponseMessage;
import org.elins.aktvtas.preferences.DeviceIdentifier;
import org.elins.aktvtas.preferences.Preferences;
import org.elins.aktvtas.sensor.SensorLog;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {
    private static final String BASE_URL = "http://har.elins.org/";

    private static HARNetworkInterface createService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(HARNetworkInterface.class);
    }

    public static void register(final Context context, String gender, int age,
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

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.code() == 201) {
                    RegisterResponse message = response.body();
                    Preferences.registerDevice(context, message.getDevice(), message.getToken());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {

            }
        });
    }


    public static void upload(Context context, final List<SensorLog> sensorLogs) {
        DeviceIdentifier deviceIdentifier = Preferences.getDeviceIdentifier(context);
        String archivePath = SensorLog.makeArchive(context, sensorLogs);
        final File archive = new File(archivePath);

        RequestBody device = RequestBody.create(MediaType.parse("text/plain"),
                deviceIdentifier.getDevice());
        RequestBody token = RequestBody.create(MediaType.parse("text/plan"),
                deviceIdentifier.getToken());
        RequestBody file = RequestBody.create(MediaType.parse("application/zip"), archive);

        HARNetworkInterface service = createService();
        Call<ResponseMessage> call = service.upload(device, token, file);

        call.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.code() == 201) {
                    SensorLog.updateStatus(sensorLogs, SensorLog.STATUS_SENT);
                }
                archive.delete();
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                archive.delete();
            }
        });
    }
}
