// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;

import java.util.concurrent.ExecutionException;

import java9.util.function.Consumer;

/**
 * Worker for renewing push notification registration. This worker could execute when APP closed.
 */
public class RegistrationRenewalWorker extends Worker {
    private ClientLogger clientLogger = new ClientLogger(RegistrationRenewalWorker.class);

    private RegistrarClient registrarClient;

    private CommunicationTokenCredential communicationTokenCredential;

    private Consumer<Throwable> exceptionHandler;

    private RegistrationKeyManager registrationKeyManager;

    public RegistrationRenewalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams,
                                     CommunicationTokenCredential communicationTokenCredential, Consumer<Throwable> exceptionHandler) {
        super(context, workerParams);
        this.registrarClient = new RegistrarClient();
        this.communicationTokenCredential = communicationTokenCredential;
        this.exceptionHandler = exceptionHandler;
        this.registrationKeyManager = RegistrationKeyManager.instance();
    }

    @NonNull
    @Override
    public Result doWork() {
        int attempts = getRunAttemptCount();
        this.clientLogger.info("RegistrationRenewalWorker execution in background: " + attempts);
        // Retry maximum 2 times. Attempts number starts from zero.
        if (attempts >= 3) {
            this.clientLogger.info("execution retry limit reached");

            //Using the exceptionHandler provided by client to handle exception.
            RuntimeException exception = new RuntimeException(
                "Registration renew request failed after "
                    + NotificationUtils.MAX_REGISTRATION_RETRY_COUNT
                    + "retries.");
            registrationKeyManager.setLastExecutionSucceeded(false);
            if (exceptionHandler != null) {
                exceptionHandler.accept(exception);
            }
            return Result.failure();
        }

        // Registration
        Data inputData = getInputData();
        String skypeUserToken;
        String deviceRegistrationToken =  inputData.getString("deviceRegistrationToken");
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            refreshCredentials();
            RegistrationKeyStore.RegistrationKeyEntry cryptoKeyToAuthKeyPair = registrationKeyManager.getLastEntry();
            registrarClient.register(
                skypeUserToken,
                deviceRegistrationToken,
                cryptoKeyToAuthKeyPair.getCryptoKey(),
                cryptoKeyToAuthKeyPair.getAuthKey());
        } catch (ExecutionException | InterruptedException e) {
            this.clientLogger.error(e.getMessage());
            return Result.retry();
        } catch (Throwable throwable) {
            this.clientLogger.error(throwable.getMessage());
            return Result.retry();
        }
        this.clientLogger.info("RegistrationRenewalWorker execution succeeded");
        registrationKeyManager.setLastExecutionSucceeded(true);
        return Result.success();
    }

    // Invoking registrationKeyManager to refresh keys. The application specific directory is only accessible using
    // context object
    private void refreshCredentials() {
        Context context = getApplicationContext();
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String dataPath = absolutePath + "/key-store";
        clientLogger.verbose("KeyStorePath is ", dataPath);
        registrationKeyManager.refreshCredentials(dataPath);
    }
}
