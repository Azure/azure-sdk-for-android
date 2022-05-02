package com.azure.android.communication.chat.implementation.notifications.fcm;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.azure.android.communication.chat.implementation.notifications.NotificationUtils;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.util.Base64Util;

import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Worker for renewing registra token. This worker could execute when APP closed. This is introduced to allow renewal
 * happen even when APP gets closed.
 * https://skype.visualstudio.com/SPOOL/_queries/query/af989357-74be-49b6-833a-d4359cd1cbd4/
 */
public class RenewTokenWorker extends Worker {
    private RegistrarClient registrarClient;

    private CommunicationTokenCredential communicationTokenCredential;

    private RenewalWorkerDataContainer renewalWorkerDataContainer;

    public RenewTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public RenewTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams,
                            CommunicationTokenCredential communicationTokenCredential) {
        super(context, workerParams);
        this.registrarClient = new RegistrarClient();
        this.communicationTokenCredential = communicationTokenCredential;
        this.renewalWorkerDataContainer = RenewalWorkerDataContainer.instance();
    }

    @NonNull
    @Override
    public Result doWork() {
        int attempts = getRunAttemptCount();
        Log.i("RenewTokenWorker", "RenewTokenWorker execution in background: " + attempts);
        // Checking retry times
        if (attempts > NotificationUtils.MAX_REGISTRATION_RETRY_COUNT) {
            Log.i("RenewTokenWorker", "execution failed");
            renewalWorkerDataContainer.setExecutionFail(true);
            return Result.failure();
        }

        // Registration
        Data inputData = getInputData();
        String skypeUserToken;
        String deviceRegistrationToken =  inputData.getString("deviceRegistrationToken");
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            Log.i("RenewTokenWorker", "get renewed push notification token: " + skypeUserToken);
            renewalWorkerDataContainer.refreshCredentials();
            registrarClient.register(
                skypeUserToken,
                deviceRegistrationToken,
                renewalWorkerDataContainer.getCurCryptoKey(),
                renewalWorkerDataContainer.getCurAuthKey());
        } catch (ExecutionException | InterruptedException e) {
            Log.i("exception", e.getMessage());
            return Result.retry();
        } catch (Throwable throwable) {
            Log.i("exception", throwable.getMessage());
            return Result.retry();
        }
        Log.i("RenewTokenWorker", "execution succeeded");
        renewalWorkerDataContainer.setExecutionFail(false);
        return Result.success();
    }
}
