package com.azure.data.azurecognitivecomputervision;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.data.azurecognitivecomputervision.models.TextOperationResult;
import com.azure.data.azurecognitivecomputervision.models.TextRecognitionMode;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Tag;

public interface ComputerVisionService {
    @Headers({ "Content-Type: application/octet-stream" })
    @POST("recognizeText")
    Call<Void> recognizeText(@Body RequestBody image, @Query("mode") TextRecognitionMode mode, @Tag HttpPipelineCallContext context);

    @Headers({ "Content-Type: application/json" })
    @GET("textOperations/{operationId}")
    Call<TextOperationResult> getTextOperationResult(@Path("operationId") String operationId, @Tag HttpPipelineCallContext context);
}
