package com.adl.genius.service.retrofit;

import com.adl.genius.entity.Response;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

@RetrofitClient(baseUrl = "${model-service.base-url}")
public interface ModelService {

    @POST("/chat")
    Response<String> chat(@Body List<String[]> qaList);
}
