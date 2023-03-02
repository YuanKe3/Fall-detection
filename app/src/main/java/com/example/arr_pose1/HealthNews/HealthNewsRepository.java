package com.example.arr_pose1.HealthNews;

import android.content.Context;

import com.example.arr_pose1.HealthNews.bean.HealthNews;
import com.example.arr_pose1.HealthNews.bean.HealthNewsResponse;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HealthNewsRepository {
  private final HealthNewsService healthNewsApi;

  private static HealthNewsRepository instance;
  public static HealthNewsRepository getInstance() {
    if (instance == null) {
      instance = new HealthNewsRepository();
    }
    return instance;
  }

  public HealthNewsRepository() {
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(HealthNewsService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    healthNewsApi = retrofit.create(HealthNewsService.class);
  }
  public List<HealthNews> getHealthNews(String apiKey, int num, int page) throws Exception {
    Call<HealthNewsResponse> call = healthNewsApi.getHealthNews(apiKey, num, page);
    Response<HealthNewsResponse> response = call.execute();
    if (response.isSuccessful()) {
      HealthNewsResponse healthNewsResponse = response.body();
      if (healthNewsResponse != null && healthNewsResponse.getCode() == 200) {
        return healthNewsResponse.getResult().getNewslist();
      }
    }
    return null;
  }
}
