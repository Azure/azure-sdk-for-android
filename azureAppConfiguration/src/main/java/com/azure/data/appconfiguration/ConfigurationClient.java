package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.RetrofitAPIClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.policy.ConfigurationCredentialsPolicy;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class ConfigurationClient {
    private final URL serviceEndpoint;
    private final ConfigurationService service;

    public ConfigurationClient(URL serviceEndpoint, String connectionString) {
        this.serviceEndpoint = serviceEndpoint;
        //
        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(credentialsPolicy(connectionString));
        //
        HttpPipelineBuilder builder = new HttpPipelineBuilder();
        builder.policies(policies.toArray(new HttpPipelinePolicy[0]));
        //
        this.service = RetrofitAPIClient.createAPIService(this.serviceEndpoint.toString(), builder.build(), ConfigurationService.class);
    }

    public ConfigurationSetting getSetting(ConfigurationSetting setting) {
        validateSetting(setting);
        HttpPipelineCallContext context = new HttpPipelineCallContext();
        Call<ConfigurationSetting> call = service.getKeyValue(setting.key(),
                setting.label(),
                null,
                null,
                null,
                null,
                context);
        //
        Response<ConfigurationSetting> configurationSettingResponse;
        try {
            configurationSettingResponse = call.execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        //
        if (configurationSettingResponse.isSuccessful()) {
            return configurationSettingResponse.body();
        } else if (configurationSettingResponse.code() == 404) {
            // todo: anuchan throw ResourceNotFoundException from the azure.core once we have that exception
            throw  new RuntimeException("ResourceNotFoundException");
        } else {
            // todo: anuchan throw HttpResponseException from the azure.core once we have that exception
            throw  new RuntimeException("HttpResponseException");
        }
    }

    public ConfigurationSetting addSetting(String key, String value) {
        Objects.requireNonNull(key);
        HttpPipelineCallContext context = new HttpPipelineCallContext();
        Call<ConfigurationSetting> call = service.setKey(key,
                null,
                new ConfigurationSetting().key(key).value(value),
                null,
                "\"*\"",
                context);
        //
        Response<ConfigurationSetting> configurationSettingResponse;
        try {
            configurationSettingResponse = call.execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (configurationSettingResponse.isSuccessful()) {
            return configurationSettingResponse.body();
        } else if (configurationSettingResponse.code() == 409) {
            // todo: anuchan throw ResourceModifiedException from the azure.core once we have that exception
            throw  new RuntimeException("ResourceModifiedException");
        } else if (configurationSettingResponse.code() == 404) {
            // todo: anuchan throw ResourceNotFoundException from the azure.core once we have that exception
            throw  new RuntimeException("ResourceNotFoundException");
        } else {
            // todo: anuchan throw HttpResponseException from the azure.core once we have that exception
            throw  new RuntimeException("HttpResponseException");
        }
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
}
