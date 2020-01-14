package com.anuchandy.learn.msal.deprecated;

import android.app.Activity;
import android.util.Log;

import com.azure.android.core.credential.TokenCredential;
import com.azure.android.core.credential.TokenRequestContext;
import com.azure.android.core.credential.AccessToken;
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

public class MsalMobileTokenCredentials implements TokenCredential {
    private final PublicClientApplication aadApp;
    private final Activity parentActivity;
    private static final String TAG = MsalMobileTokenCredentials.class.getSimpleName();

    public MsalMobileTokenCredentials(Activity parentActivity, PublicClientApplication aadApp) {
        this.parentActivity = parentActivity;
        this.aadApp = aadApp;
    }

    @Override
    public AccessToken getToken(final TokenRequestContext request) {
        SyncTask<AccessToken> tokenRetrieveTask = new SyncTask<>(retrieveToken(request));
        try {
            return tokenRetrieveTask.getResult();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private SyncTask.Work<AccessToken> retrieveToken(final TokenRequestContext request) {
        return output -> aadApp.getAccounts(accounts -> {
            if (!accounts.isEmpty()) {
                aadApp.acquireTokenSilentAsync(request.getScopes().toArray(new String[0]), accounts.get(0), new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authResult) {
                        Log.d(TAG, "[acquireTokenSilentAsync]: Successfully authenticated.");
                        output.setValue(new AccessToken(authResult.getAccessToken(),
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
                        output.setError(exception);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "[acquireTokenSilentAsync]: User cancelled login.");
                        output.setError(new RuntimeException("User cancelled login."));
                    }
                });
            } else {
                aadApp.acquireToken(parentActivity, request.getScopes().toArray(new String[0]), new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authResult) {
                        Log.d(TAG, "[acquireToken]: Successfully authenticated.");
                        output.setValue(new AccessToken(authResult.getAccessToken(),
                                toOffsetDateTime(authResult.getExpiresOn())));
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.d(TAG, "[acquireToken]: Authentication failed: " + exception.toString());
                        if (exception instanceof MsalClientException) {
                        } else if (exception instanceof MsalServiceException) {
                        }
                        output.setError(exception);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "User cancelled login.");
                        output.setError(new RuntimeException("User cancelled login."));
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
