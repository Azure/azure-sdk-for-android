package com.azure.data.azurecognitivecomputervision;

import com.azure.core.exception.DecodeException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.RetrofitAPIClient;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.OkHttpRequest;
import com.azure.core.implementation.http.OkHttpResponse;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.data.azurecognitivecomputervision.models.ComputerVisionErrorException;
import com.azure.data.azurecognitivecomputervision.models.RecognizeTextHeaders;
import com.azure.data.azurecognitivecomputervision.models.RecognizeTextHeadersResponse;
import com.azure.data.azurecognitivecomputervision.models.TextOperationResult;
import com.azure.data.azurecognitivecomputervision.models.TextRecognitionMode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ComputerVisionClient {
    private static final String COMPUTER_VISION_ENDPOINT = "vision/v2.0/";

    private final String serviceEndpoint;
    private final SerializerAdapter serializerAdapter;
    private final ComputerVisionService service;

    public ComputerVisionClient(String serviceEndpoint, String subscriptionKey) throws MalformedURLException {
        this.serviceEndpoint = sanitizeServiceEndpoint(serviceEndpoint) + COMPUTER_VISION_ENDPOINT;
        this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        //
        UrlBuilder urlBuilder = UrlBuilder.parse(new URL(serviceEndpoint));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("Host", urlBuilder.host());
        httpHeaders.put("Ocp-Apim-Subscription-Key", subscriptionKey);
        //
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new AddHeadersPolicy(httpHeaders));
        //
        HttpPipelineBuilder builder = new HttpPipelineBuilder();
        builder.policies(policies.toArray(new HttpPipelinePolicy[0]));
        //
        this.service = RetrofitAPIClient.createAPIService(this.serviceEndpoint, serializerAdapter, builder.build(), ComputerVisionService.class);
    }

    public RecognizeTextHeadersResponse recognizeTextWithResponse(byte[] image) {
        RequestBody requestBody = RequestBody.create(image, MediaType.parse("application/octet-stream"));
        retrofit2.Call<Void> call = service.recognizeText(requestBody, TextRecognitionMode.PRINTED, new HttpPipelineCallContext());
        //
        retrofit2.Response<Void> callResponse = executeCall(call);
        //
        RecognizeTextHeaders recognizeTextHeaders;
        if (callResponse.isSuccessful()) {
            HttpHeaders httpHeaders = fromOkHttpHeaders(callResponse.headers());
            HttpRequest httpRequest = new OkHttpRequest(callResponse.raw().request());
            try {
                byte[] serializedHeaders = this.serializerAdapter.serialize(httpHeaders, SerializerEncoding.JSON);
                recognizeTextHeaders = this.serializerAdapter.deserialize(serializedHeaders, RecognizeTextHeaders.class, SerializerEncoding.JSON);

            } catch (IOException ioe) {
                throw new DecodeException("response headers decoding failed", new OkHttpResponse(callResponse.raw(), httpRequest), ioe);
            }
            return new RecognizeTextHeadersResponse(httpRequest, callResponse.code(), httpHeaders, callResponse.body(), recognizeTextHeaders);
        } else {
            Map<Integer, Class<? extends HttpResponseException>> exceptionMapping = new HashMap<>();
            throw createException(exceptionMapping, callResponse);
        }
    }

    public Response<TextOperationResult> getTextOperationResultWithResponse(String operationId) {
        retrofit2.Call<TextOperationResult> call = service.getTextOperationResult(operationId, new HttpPipelineCallContext());
        //
        retrofit2.Response<TextOperationResult> callResponse = executeCall(call);
        //
        if (callResponse.isSuccessful()) {
            return new SimpleResponse<>(new OkHttpRequest(callResponse.raw().request()), callResponse.code(), fromOkHttpHeaders(callResponse.headers()), callResponse.body());
        } else {
            Map<Integer, Class<? extends HttpResponseException>> exceptionMapping = new HashMap<>();
            exceptionMapping.put(-1, ComputerVisionErrorException.class);
            throw createException(exceptionMapping, callResponse);
        }
    }

    private static <T> retrofit2.Response<T> executeCall(retrofit2.Call<T> call) {
        try {
            return call.execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private RuntimeException createException(Map<Integer, Class<? extends HttpResponseException>> exceptionMapping, retrofit2.Response<?> callResponse) {
        return ImplUtils.createException(exceptionMapping, callResponse.errorBody(), new OkHttpResponse(callResponse.raw(), new OkHttpRequest(callResponse.raw().request())), this.serializerAdapter);
    }

    private static HttpHeaders fromOkHttpHeaders(okhttp3.Headers headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String headerName : headers.names()) {
            httpHeaders.put(headerName, headers.get(headerName));
        }
        return httpHeaders;
    }

    //TODO: Add more rules to improve sanitization
    private String sanitizeServiceEndpoint(String serviceEndpoint) {
        return serviceEndpoint.endsWith("/") ? serviceEndpoint : serviceEndpoint + "/";
    }
}
