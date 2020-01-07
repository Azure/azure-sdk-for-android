package com.azure.android.core.http.rest;

/**
 * REST response with a strongly-typed header and content.
 *
 * @param <H> The type of the response headers.
 * @param <T> The type of the response value, available from {@link Response#getValue()}.
 */
public class ResponseBase<H, T> extends Response<T> {
    private final H deserializedHeaders;

    /**
     * Creates a {@link ResponseBase}.
     *
     * @param request The HTTP request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     *
     * @param value The deserialized value of the HTTP response.
     */
    /**
     * Creates REST response.
     *
     * @param request the request that resulted in this response
     * @param statusCode the http response status code
     * @param headers the http response header
     * @param value the http response content as strongly typed instance
     * @param deserializedHeaders the HTTP response headers as strongly typed instance
     */
    public ResponseBase(okhttp3.Request request, int statusCode, okhttp3.Headers headers,
                        T value,
                        H deserializedHeaders) {
        super(request, statusCode, headers, value);
        this.deserializedHeaders = deserializedHeaders;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type, {@code H}.
     *
     * @return An instance of header type {@code H}, deserialized from the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }
}
