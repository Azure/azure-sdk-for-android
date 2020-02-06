// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.exception.HttpResponseException;

/**
 * A {@code BlobStorageException} is thrown whenever Azure Storage successfully returns an error code that is not
 * 200-level. Users can inspect the status code and error code to determine the cause of the error response. The
 * exception message may also contain more detailed information depending on the type of error. The user may also
 * inspect the raw HTTP response or call toString to get the full payload of the error response if present. Note that
 * even some expected "errors" will be thrown as a {@code BlobStorageException}. For example, some users may perform a
 * getProperties request on an entity to determine whether it exists or not. If it does not exists, an exception will be
 * thrown even though this may be considered an expected indication of absence in this case.
 *
 * <p><strong>Sample Code</strong></p>
 * <p>For more samples, please see the <a href="https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java">sample
 * file</a></p>
 */
public final class BlobStorageException extends HttpResponseException {
    /**
     * Constructs a {@code BlobStorageException}.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the error code of the exception.
     */
    public BlobStorageException(String message, okhttp3.Response response, Object value) {
        super(message, response, value);
    }

    /**
     * Constructs a {@code BlobStorageException}.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public BlobStorageException(String message, okhttp3.Response response) {
        super(message, response);
    }

    /**
     * @return The error code returned by the service.
     */
    public BlobErrorCode getErrorCode() {
        return BlobErrorCode.fromString(super.getResponse().header("x-ms-error-code"));
    }

    /**
     * @return The message returned by the service.
     */
    public String getServiceMessage() {
        return super.getMessage();
    }

    /**
     * @return The status code on the response.
     */
    public int getStatusCode() {
        return super.getResponse().code();
    }
}
