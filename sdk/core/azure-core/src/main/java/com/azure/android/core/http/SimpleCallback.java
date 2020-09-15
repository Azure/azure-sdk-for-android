package com.azure.android.core.http;

/**
 * Callback to receive a service operation result.
 *
 * @param <U> The type of the result.
 */
public interface SimpleCallback<U> {
    /**
     * The method to call on a successful result.
     *
     * @param value    The value.
     * @param response The response.
     */
    void onSuccess(U value, okhttp3.Response response);

    /**
     * The method to call on failure.
     *
     * @param t A throwable with the failure details.
     * @param response The response.
     */
    void onFailure(Throwable t, okhttp3.Response response);
}
