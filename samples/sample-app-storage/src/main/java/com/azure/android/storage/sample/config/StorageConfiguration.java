package com.azure.android.storage.sample.config;

import android.content.Context;

import com.azure.android.storage.sample.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;

public class StorageConfiguration {
    @SerializedName("blob_service_url")
    String mBlobServiceUrl;

    @SerializedName("container_name")
    String mContainerName;

    @SerializedName("sas_token")
    String mSasToken;

    public String getBlobServiceUrl() {
        return mBlobServiceUrl;
    }

    public String getContainerName() {
        return mContainerName;
    }

    public String getSasToken() {
        return mSasToken;
    }

    public static StorageConfiguration create(Context context) {
        byte[] buffer;
        try(InputStream configStream = context.getResources().openRawResource(R.raw.storage_config)) {
            buffer = new byte[configStream.available()];
            configStream.read(buffer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to open the configuration file - 'R.raw.storage_config'.", e);
        }
        final String config = new String(buffer);
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(config, StorageConfiguration.class);
    }
}
