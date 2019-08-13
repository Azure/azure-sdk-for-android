package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.RetrofitAPIClient;
import com.azure.core.implementation.http.OkHttpRequest;
import com.azure.core.implementation.http.OkHttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.policy.ConfigurationCredentialsPolicy;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigurationClient {
    private final URL serviceEndpoint;
    private final SerializerAdapter serializerAdapter;
    private final ConfigurationService service;

    public ConfigurationClient(URL serviceEndpoint, String connectionString) {
        this.serviceEndpoint = serviceEndpoint;
        this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        //
        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(credentialsPolicy(connectionString));
        //
        HttpPipelineBuilder builder = new HttpPipelineBuilder();
        builder.policies(policies.toArray(new HttpPipelinePolicy[0]));
        //
        this.service = RetrofitAPIClient.createAPIService(this.serviceEndpoint.toString(), serializerAdapter, builder.build(), ConfigurationService.class);
    }

    public Response<ConfigurationSetting> getSettingWithResponse(ConfigurationSetting setting) {
        validateSetting(setting);
        retrofit2.Call<ConfigurationSetting> call = service.getKeyValue(setting.key(),
            setting.label(),
            null,
            null,
            null,
            null,
            new HttpPipelineCallContext());
        //
        retrofit2.Response<ConfigurationSetting> callResponse = executeCall(call);
        //
        if (callResponse.isSuccessful()) {
            return new SimpleResponse<>(new OkHttpRequest(callResponse.raw().request()), callResponse.code(), fromOkHttpHeaders(callResponse.headers()), callResponse.body());
        } else {
            Map<Integer, Class<? extends HttpResponseException>> exceptionMapping = new HashMap<>();
            exceptionMapping.put(404, ResourceNotFoundException.class);
            throw createException(exceptionMapping, callResponse);
        }
    }

    public Response<ConfigurationSetting> addSettingWithResponse(String key, String value) {
        Objects.requireNonNull(key);
        HttpPipelineCallContext context = new HttpPipelineCallContext();
        retrofit2.Call<ConfigurationSetting> call = service.setKey(key,
            null,
            new ConfigurationSetting().key(key).value(value),
            null,
            "\"*\"",
            context);
        //
        retrofit2.Response<ConfigurationSetting> callResponse = executeCall(call);
        //
        if (callResponse.isSuccessful()) {
            return new SimpleResponse<>(new OkHttpRequest(callResponse.raw().request()), callResponse.code(), fromOkHttpHeaders(callResponse.headers()), callResponse.body());
        } else {
            Map<Integer, Class<? extends HttpResponseException>> exceptionMapping = new HashMap<>();
            exceptionMapping.put(409, ResourceModifiedException.class);
            exceptionMapping.put(404, ResourceNotFoundException.class);
            throw createException(exceptionMapping, callResponse);
        }
    }

    public ConfigurationSetting getSetting(ConfigurationSetting setting) {
        return this.getSettingWithResponse(setting).value();
    }

    public ConfigurationSetting addSetting(String key, String value) {
        return this.addSettingWithResponse(key, value).value();
    }

    private static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.key() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    private static HttpPipelinePolicy credentialsPolicy(String connectionString) {
        ConfigurationClientCredentials credentials;
        try {
            credentials = new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException ive) {
            throw new RuntimeException(ive);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        return new ConfigurationCredentialsPolicy(credentials);
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
}
