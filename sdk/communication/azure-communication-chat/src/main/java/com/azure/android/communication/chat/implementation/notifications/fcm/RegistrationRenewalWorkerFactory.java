// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

import com.azure.android.communication.common.CommunicationTokenCredential;

import java9.util.function.Consumer;

public class RegistrationRenewalWorkerFactory extends WorkerFactory {
    private CommunicationTokenCredential communicationTokenCredential;

    private Consumer<Throwable> exceptionHandler;

    public RegistrationRenewalWorkerFactory(CommunicationTokenCredential communicationTokenCredential, Consumer<Throwable> exceptionHandler) {
        this.communicationTokenCredential = communicationTokenCredential;
        this.exceptionHandler = exceptionHandler;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {

        return new RegistrationRenewalWorker(appContext, workerParameters, communicationTokenCredential, exceptionHandler);
    }
}
