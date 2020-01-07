package com.azure.android.core.http.rest;

/**
 * REST response with a strongly-typed content.
 *
 * @param <T> The type of the response content, available from {@link #getValue()}.
 */
public class Response<T> {
    private final okhttp3.Request request;
    private final int statusCode;
    private final okhttp3.Headers headers;
    private final T value;

    /**
     * Creates REST response.
     *
     * @param request the request that resulted in this response
     * @param statusCode the http response status code
     * @param headers the http response header
     * @param value the http response content as strongly typed instance
     */
    public Response(okhttp3.Request request, int statusCode, okhttp3.Headers headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    okhttp3.Request getRequest() {
        return this.request;
    }

    /**
     * Gets the HTTP response status code.
     *
     * @return The status code of the HTTP response.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    public okhttp3.Headers getHeaders() {
        return this.headers;
    }

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    public T getValue() {
        return this.value;
    }
}
