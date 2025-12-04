package com.example.medibookandroid.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FCMService {
    @POST("v1/projects/medibookandroid/messages:send")
    Call<FCMResponse> sendNotification(
            @Header("Authorization") String authToken, // Truyền token vào đây
            @Body FCMRequestV1 body
    );
}