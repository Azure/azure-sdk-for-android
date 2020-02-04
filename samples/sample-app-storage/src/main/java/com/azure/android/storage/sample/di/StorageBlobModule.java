package com.azure.android.storage.sample.di;

import com.azure.android.storage.blob.StorageBlobClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageBlobModule {
    String baseUrl;

    public StorageBlobModule(String blobBaseUrl) {
        this.baseUrl = blobBaseUrl;
    }

    @Provides
    @Singleton
    StorageBlobClient provideStorageBlobClient() {
        return new StorageBlobClient.Builder()
                .setBlobServiceUrl(this.baseUrl)
                .build();
    }
}
