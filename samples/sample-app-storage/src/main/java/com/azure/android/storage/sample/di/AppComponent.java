package com.azure.android.storage.sample.di;

import com.azure.android.storage.sample.ListAndDownloadBlobsActivity;
import com.azure.android.storage.sample.UploadFileActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={StorageBlobModule.class})
public interface AppComponent {
    void inject(ListAndDownloadBlobsActivity activity);
    void inject(UploadFileActivity activity);
}
