package com.anuchandy.learn.msal;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.azure.android.core.credential.AccessToken;
import com.azure.android.core.util.tokenrequest.TokenResponseCallback;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.Date;

class MsalClient {
    private static final String TAG = MsalClient.class.getSimpleName();

    private MsalClient() {}

    public static void signIn(@NonNull final PublicClientApplication aadApp,
                       @NonNull final Activity activity,
                       @NonNull final String[] scopes,
                       @NonNull final TokenResponseCallback callback) {
        aadApp.getAccounts(accounts -> {
            if (!accounts.isEmpty()) {
                aadApp.acquireTokenSilentAsync(scopes, accounts.get(0), new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authResult) {
                        Log.d(TAG, "[acquireTokenSilentAsync]: Successfully authenticated.");
                        callback.onToken(new AccessToken(authResult.getAccessToken(),
                                toOffsetDateTime(authResult.getExpiresOn())));
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.d(TAG, "[acquireTokenSilentAsync]: Authentication failed: " + exception.toString());
                        if (exception instanceof MsalClientException) {
                        } else if (exception instanceof MsalServiceException) {
                            /* Exception when communicating with the STS, likely config issue */
                        } else if (exception instanceof MsalUiRequiredException) {
                            /* Tokens expired or no session, retry with interactive */
                        }
                        callback.onError(exception);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "[acquireTokenSilentAsync]: User cancelled login.");
                        callback.onError(new RuntimeException("User cancelled login."));
                    }
                });
            } else {
                aadApp.acquireToken(activity, scopes, new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authResult) {
                        Log.d(TAG, "[acquireToken]: Successfully authenticated.");
                        callback.onToken(new AccessToken(authResult.getAccessToken(),
                                toOffsetDateTime(authResult.getExpiresOn())));
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.d(TAG, "[acquireToken]: Authentication failed: " + exception.toString());
                        if (exception instanceof MsalClientException) {
                        } else if (exception instanceof MsalServiceException) {
                        }
                        callback.onError(exception);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "User cancelled login.");
                        callback.onError(new RuntimeException("User cancelled login."));
                    }
                });
            }
        });
    }

    private static OffsetDateTime toOffsetDateTime(Date utcDte) {
        return DateTimeUtils
                .toInstant(utcDte)
                .atOffset(ZoneOffset.UTC);
    }
}
