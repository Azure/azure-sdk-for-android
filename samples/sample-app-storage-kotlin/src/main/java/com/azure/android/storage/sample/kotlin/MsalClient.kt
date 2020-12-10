package com.azure.android.storage.sample.kotlin

import android.app.Activity
import android.util.Log
import com.azure.android.core.credential.AccessToken
import com.azure.android.core.credential.TokenResponseCallback
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import java.util.*

internal object MsalClient {
    private val TAG = MsalClient::class.java.simpleName
    fun signIn(aadApp: IMultipleAccountPublicClientApplication,
               activity: Activity,
               scopes: Array<String>,
               callback: TokenResponseCallback) {
        aadApp.getAccounts(object : LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                if (result.isNotEmpty()) {
                    val authority = aadApp.configuration.defaultAuthority.authorityURL.toString()
                    aadApp.acquireTokenSilentAsync(scopes, result[0], authority, object : SilentAuthenticationCallback {
                        override fun onSuccess(authResult: IAuthenticationResult) {
                            Log.d(TAG, "[acquireTokenSilentAsync]: Successfully authenticated.")
                            callback.onToken(AccessToken(authResult.accessToken,
                                toOffsetDateTime(authResult.expiresOn)))
                        }

                        override fun onError(exception: MsalException) {
                            Log.d(TAG, "[acquireTokenSilentAsync]: Authentication failed: $exception")
                            when (exception) {
                                is MsalClientException -> {
                                    // Exception thrown for general errors that are local to the MSAL library. For more
                                    // information, see: https://javadoc.io/doc/com.microsoft.identity.client/msal/0.1.1/com/microsoft/identity/client/MsalClientException.html
                                }
                                is MsalServiceException -> {
                                    // Exception thrown when communicating with the STS, likely because of a configuration
                                    // issue. For more information, see: https://javadoc.io/static/com.microsoft.identity.client/msal/0.1.1/com/microsoft/identity/client/MsalServiceException.html
                                }
                                is MsalUiRequiredException -> {
                                    // Exception thrown when auth token is expired or there is no active session, must retry
                                    // with interactive login. For more information, see: https://javadoc.io/static/com.microsoft.identity.client/msal/0.1.1/com/microsoft/identity/client/MsalUiRequiredException.html
                                }
                            }
                            callback.onError(exception)
                        }
                    })
                } else {
                    aadApp.acquireToken(activity, scopes, object : AuthenticationCallback {
                        override fun onSuccess(authResult: IAuthenticationResult) {
                            Log.d(TAG, "[acquireToken]: Successfully authenticated.")
                            callback.onToken(AccessToken(authResult.accessToken,
                                toOffsetDateTime(authResult.expiresOn)))
                        }

                        override fun onError(exception: MsalException) {
                            Log.d(TAG, "[acquireToken]: Authentication failed: $exception")
                            if (exception is MsalClientException) {
                            } else if (exception is MsalServiceException) {
                            }
                            callback.onError(exception)
                        }

                        override fun onCancel() {
                            Log.d(TAG, "User cancelled login.")
                            callback.onError(RuntimeException("User cancelled login."))
                        }
                    })
                }
            }

            override fun onError(exception: MsalException) {
                Log.e(TAG, "Exception found when trying to sign in.", exception)
            }
        })
    }

    private fun toOffsetDateTime(utcDte: Date): OffsetDateTime {
        return DateTimeUtils
            .toInstant(utcDte)
            .atOffset(ZoneOffset.UTC)
    }
}
