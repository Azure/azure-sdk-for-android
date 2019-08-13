package com.azure.core;

import retrofit2.Call;
import retrofit2.http.GET;

//
interface HttpBinService {
    @GET("anything")
    Call<HttpBinJSON> getAnything();
}
