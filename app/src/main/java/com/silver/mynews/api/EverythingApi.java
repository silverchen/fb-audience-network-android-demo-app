package com.silver.mynews.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.silver.mynews.helpers.DateDeserializer;
import com.silver.mynews.model.Everything;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.silver.mynews.api.Constants.API_KEY;
import static com.silver.mynews.api.Constants.BASE_URL;

public class EverythingApi {
    private static ApiInterface mEverythingApi;
    private static final int TIMEOUT = 10;

    public static ApiInterface ApiClient() {
        if (mEverythingApi == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);

//            Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClientBuilder.build())
//                .build();

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
            Gson gson = gsonBuilder.create();
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClientBuilder.build())
                .build();

            mEverythingApi = retrofit.create(ApiInterface.class);
        }

        return mEverythingApi;
    }

    public interface ApiInterface {
        @GET("everything?apiKey=" + API_KEY)
        Call<Everything> get(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("domains") String domains,
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("q") String query
        );
    }
}