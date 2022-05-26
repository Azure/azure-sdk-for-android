// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;
import com.azure.android.core.util.Base64Util;

import java.util.concurrent.ExecutionException;

import javax.crypto.SecretKey;

/**
 * Worker for renewing registration. This worker could execute when APP closed.
 */
public class RegistrationRenewalWorker extends Worker {
    private ClientLogger clientLogger = new ClientLogger(RegistrationRenewalWorker.class);

    private RegistrarClient registrarClient;

    private CommunicationTokenCredential communicationTokenCredential;

    private RegistrationDataContainer registrationDataContainer;

    public RegistrationRenewalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public RegistrationRenewalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams,
                                     CommunicationTokenCredential communicationTokenCredential) {
        super(context, workerParams);

        this.communicationTokenCredential = communicationTokenCredential;
        this.registrationDataContainer = RegistrationDataContainer.instance();
    }

    @NonNull
    @Override
    public Result doWork() {
        this.registrarClient = new RegistrarClient();
        int attempts = getRunAttemptCount();
        Log.i("RegistrationRenewal", "RegistrationRenewalWorker execution in background: " + attempts);
        this.clientLogger.info("RegistrationRenewalWorker execution in background: " + attempts);
        // Retry maximum 2 times. Attempts number starts from zero.
        if (attempts >= 3) {
            Log.i("RegistrationRenewal", "execution retry limit reached");
            this.clientLogger.info("execution retry limit reached");
            registrationDataContainer.setExecutionFail(true);
            return Result.failure();
        }

        // Registration
        Data inputData = getInputData();
        String skypeUserToken;
        String deviceRegistrationToken =  inputData.getString("deviceRegistrationToken");
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            refreshCredentials();
            Pair<SecretKey, SecretKey> keyPair = registrationDataContainer.getLastPair();
            Log.v("renew in worker", "skype: " + skypeUserToken + ", \ndevice: " + deviceRegistrationToken + ", \ncrypto: " +
                Base64Util.encodeToString(keyPair.first.getEncoded()) + ",\n auth:" + Base64Util.encodeToString(keyPair.second.getEncoded()));
            registrarClient.register(
                skypeUserToken,
                deviceRegistrationToken,
                keyPair.first,
                keyPair.second);
        } catch (ExecutionException | InterruptedException e) {
            Log.e("exception", e.getMessage());
            this.clientLogger.error(e.getMessage());
            return Result.retry();
        } catch (Throwable throwable) {
            Log.e("exception", throwable.getMessage());
            this.clientLogger.error(throwable.getMessage());
            return Result.retry();
        }
        Log.i("RegistrationRenewal", "execution succeeded");
        this.clientLogger.info("RegistrationRenewalWorker execution succeeded");
        registrationDataContainer.setExecutionFail(false);
        return Result.success();
    }

    // Invoking registrationDataContainer to refresh keys. The application specific directory is only accessible using
    // context
    private void refreshCredentials() {
        Context context = getApplicationContext();
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String dataPath = absolutePath + "/key-store.jks";
        Log.d("KeyStorePath", dataPath);
        registrationDataContainer.refreshCredentials(dataPath);
    }
}
