package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Tag;

interface ConfigurationService {
    @GET("kv/{key}")
//    @ExpectedResponses({200})
//    @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
//    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Call<ConfigurationSetting> getKeyValue(@Path("key") String key,
                                           @Query("label") String label,
                                           @Query("$select") String fields,
                                           @Header("Accept-Datetime") String acceptDatetime,
                                           @Header("If-Match") String ifMatch,
                                           @Header("If-None-Match") String ifNoneMatch,
                                           @Tag HttpPipelineCallContext context);
}
