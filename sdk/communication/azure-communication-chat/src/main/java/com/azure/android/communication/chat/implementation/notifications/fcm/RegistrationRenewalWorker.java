// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.azure.android.communication.common.CommunicationTokenCredential;

import java.util.concurrent.ExecutionException;

public class RegistrationRenewalWorker extends Worker {
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
        // Checking retry times
        if (attempts > 0) {
            Log.i("RegistrationRenewal", "execution failed");
            registrationDataContainer.setExecutionFail(true);
            return Result.failure();
        }

        // Registration
        Data inputData = getInputData();
        String skypeUserToken;
        String deviceRegistrationToken =  inputData.getString("deviceRegistrationToken");
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            Log.i("RegistrationRenewal", "get renewed push notification token: " + skypeUserToken);
            refreshCredentials();
//            Log.i("renew in worker", "skype: " + skypeUserToken + ", \ndevice: " + deviceRegistrationToken + ", \ncrypto: " + Base64Util.encodeToString(renewalWorkerDataContainer.getCurCryptoKey().getEncoded()) + ",\n auth:" + Base64Util.encodeToString(renewalWorkerDataContainer.getCurAuthKey().getEncoded()));
            registrarClient.register(
                skypeUserToken,
                deviceRegistrationToken,
                registrationDataContainer.getSecreteKey(RegistrationDataContainer.curCryptoKeyAlias),
                registrationDataContainer.getSecreteKey(RegistrationDataContainer.curAuthKeyAlias));
        } catch (ExecutionException | InterruptedException e) {
            Log.i("exception", e.getMessage());
            return Result.retry();
        } catch (Throwable throwable) {
            Log.i("exception", throwable.getMessage());
            return Result.retry();
        }
        Log.i("RegistrationRenewal", "execution succeeded");
        registrationDataContainer.setExecutionFail(false);
        return Result.success();
    }

    private void refreshCredentials() {
        Context context = getApplicationContext();
        String absolutePath = context.getFilesDir().getAbsolutePath();
        Log.i("FilesPath", absolutePath);
        String dataPath = absolutePath + "/key-store";
        Log.i("FilePath", dataPath);
        registrationDataContainer.refreshCredentials(dataPath);
    }
}
