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

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java9.util.function.Consumer;
import lombok.Setter;

public class RenewTokenWorker extends Worker {
    private RegistrarClient registrarClient;

    private CommunicationTokenCredential communicationTokenCredential;

    private SecretKey cryptoKey;

    private SecretKey authKey;

    private static final int KEY_SIZE = 256;

    public RenewTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public RenewTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams,
                            CommunicationTokenCredential communicationTokenCredential) {
        super(context, workerParams);
        this.registrarClient = new RegistrarClient();
        this.communicationTokenCredential = communicationTokenCredential;
    }

    @NonNull
    @Override
    public Result doWork() {
        int attempts = getRunAttemptCount();
        Log.i("RenewTokenWorker", "RenewTokenWorker execution in background: " + attempts);
        // Checking retry times
        if (attempts > NotificationUtils.MAX_REGISTRATION_RETRY_COUNT) {
            Data failedData = new Data.Builder().putBoolean("success", false).build();
            return Result.failure(failedData);
        }

        // Registration
        Data inputData = getInputData();
        String skypeUserToken;
        String deviceRegistrationToken =  inputData.getString("deviceRegistrationToken");
        try {
            skypeUserToken = communicationTokenCredential.getToken().get().getToken();
            Log.i("renew token", "get renewed push notification token: " + skypeUserToken);
            refreshEncryptionKeys();
            registrarClient.register(
                skypeUserToken,
                deviceRegistrationToken,
                cryptoKey,
                authKey);
        } catch (ExecutionException | InterruptedException e) {
            Log.i("exception", e.getMessage());
            return Result.retry();
        } catch (Throwable throwable) {
            Log.i("exception", throwable.getMessage());
            return Result.retry();
        }
        //Returning processing result and renewed keys
        String strCryptoKey = Base64Util.encodeToString(cryptoKey.getEncoded());
        String strAuthKey = Base64Util.encodeToString(authKey.getEncoded());
        Data successData = new Data.Builder()
            .putBoolean("success", true).putString("strCryptoKey", strCryptoKey).putString("strAuthKey", strAuthKey).build();
        return Result.success(successData);
    }

    private void refreshEncryptionKeys() throws Throwable {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEY_SIZE);

        cryptoKey = keyGenerator.generateKey();
        authKey = keyGenerator.generateKey();
    }
}
