package com.example.sabana.fcmsendservice;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RestSendCallService {

    @POST("/fcm/send")
    Call<SendResult> send(
            @Header("Authorization") String auth,
            @Body FcmSend fcmSend);
}
