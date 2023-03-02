package com.example.arr_pose1.HealthNews;

import com.example.arr_pose1.HealthNews.bean.HealthNewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HealthNewsService {
  String BASE_URL = "https://apis.tianapi.com";
  @GET("/health/index")
  Call<HealthNewsResponse> getHealthNews(
          @Query("key") String apiKey,
          @Query("num") int num,
          @Query("page") int page
  );
}
