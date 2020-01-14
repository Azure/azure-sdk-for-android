package com.anuchandy.learn.msal.di;

import com.anuchandy.learn.msal.ContainerBlobsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={StorageBlobModule.class})
public interface AppComponent {
    void inject(ContainerBlobsActivity activity);
}