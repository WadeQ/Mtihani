package com.wadektech.mtihanirevise.fragments;

import com.wadektech.mtihanirevise.notification.MyResponse;
import com.wadektech.mtihanirevise.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
               "Content-Type: application/json",
               "Authorization: key = AAAAUIbBytw:APA91bEYM6HatCw6zpBSzlqAYnik3ByDP4mhnlaCtE8fEkEjNfKIyceNC1_A58KZDIX1_EcOONfpf6JLIFvHIjLuRvjvaDp0a6fp8Ke8btnQjeYwCu1BpdnrlaQ9JG9iSsEcxfl5uTT-"
            }
    )
   @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender sender);
}
