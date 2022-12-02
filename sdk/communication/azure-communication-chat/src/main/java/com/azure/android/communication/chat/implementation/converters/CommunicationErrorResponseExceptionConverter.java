// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.implementation.converters;

import com.azure.android.communication.chat.implementation.models.CommunicationErrorResponseException;
import com.azure.android.communication.chat.models.ChatError;
import com.azure.android.communication.chat.models.ChatErrorResponseException;

/**
 * A converter between {@link CommunicationErrorResponseException} and
 * {@link ChatErrorResponseException}.
 */
public final class CommunicationErrorResponseExceptionConverter {
    /**
     * Maps from {@link CommunicationErrorResponseException} to {@link ChatErrorResponseException}.
     * Keeps other kind of exceptions as it is.
     */
    public static RuntimeException convert(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        if (throwable instanceof CommunicationErrorResponseException) {
            // translate CommunicationErrorResponseException to ChatErrorResponseException
            ChatError error = null;
            CommunicationErrorResponseException exception = (CommunicationErrorResponseException) throwable;
            if (exception.getValue() != null) {
                error = ChatErrorConverter.convert(exception.getValue().getError());
            }
            return new ChatErrorResponseException(exception.getMessage(), exception.getResponse(), error);
        } else if (throwable instanceof RuntimeException) {
            // avoid double-wrapping for already unchecked exception
            return (RuntimeException) throwable;
        } else {
            // wrap checked exception in a unchecked runtime exception
            return new RuntimeException(throwable);
        }
    }

    private CommunicationErrorResponseExceptionConverter() {
    }
}