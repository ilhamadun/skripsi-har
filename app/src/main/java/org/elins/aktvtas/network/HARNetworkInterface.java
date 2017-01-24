package org.elins.aktvtas.network;

import org.elins.aktvtas.network.response.RegisterResponse;
import org.elins.aktvtas.network.response.ResponseMessage;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HARNetworkInterface {
    @FormUrlEncoded
    @POST("/subject/register")
    Call<RegisterResponse> register(
            @Field("user_gender") String gender,
            @Field("user_age") Integer age,
            @Field("accelerometer") Boolean accelerometer,
            @Field("ambient_temperature") Boolean ambientTemperature,
            @Field("gravity") Boolean gravity,
            @Field("gyroscope") Boolean gyroscope,
            @Field("light") Boolean light,
            @Field("linear_accelerometer") Boolean linearAccelerometer,
            @Field("magnetic_field") Boolean magneticField,
            @Field("orientation") Boolean orientation,
            @Field("pressure") Boolean pressure,
            @Field("proximity") Boolean proximity,
            @Field("relative_humidity") Boolean relativeHumidity,
            @Field("rotation_vector") Boolean rotationVector,
            @Field("temperature") Boolean temperature
    );

    @Multipart
    @POST("/log/upload")
    Call<ResponseMessage> upload(
            @Part("device") RequestBody device,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part archive
    );
}
