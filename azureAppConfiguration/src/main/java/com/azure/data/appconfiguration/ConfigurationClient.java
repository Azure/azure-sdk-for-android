package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipelineBuilder;
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
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);
        Call<ConfigurationSetting> call = service.getKeyValue(setting.key(), setting.label(), null, null, null, null);

        try {
            return call.execute().body();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
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
