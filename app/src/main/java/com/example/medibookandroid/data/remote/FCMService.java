package com.example.medibookandroid.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FCMService {
    // API mới có dạng: v1/projects/{project_id}/messages:send
    // Bạn thay "medibookandroid" bằng Project ID chuẩn của bạn
    @POST("v1/projects/medibookandroid/messages:send")
    Call<FCMResponse> sendNotification(
            @Header("Authorization") String authToken, // Truyền token vào đây
            @Body FCMRequestV1 body
    );
}