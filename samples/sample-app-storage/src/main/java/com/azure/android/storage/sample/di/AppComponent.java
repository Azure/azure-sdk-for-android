package com.azure.android.storage.sample.di;

import com.azure.android.storage.sample.ContainerBlobsActivity;
import com.azure.android.storage.sample.ProcessFileActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={StorageBlobModule.class})
public interface AppComponent {
    void inject(ContainerBlobsActivity activity);
    void inject(ProcessFileActivity activity);
}
