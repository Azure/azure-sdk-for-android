package com.azure.android.storage.sample.di;

import com.azure.android.storage.blob.StorageBlobAsyncClient;

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
    StorageBlobAsyncClient provideStorageBlobClient() {
        return new StorageBlobAsyncClient.Builder("com.azure.android.storage.sample")
                .setBlobServiceUrl(this.baseUrl)
                .build();
    }
}
