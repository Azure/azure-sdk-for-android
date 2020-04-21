// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.transfer;

import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.CoreUtil;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

final class Util {
    private Util() {
        // Empty constructor to prevent instantiation of this class.
    }

    /**
     * The exception from storage blob service usually adheres to a specific format
     * described by {@link BlobServiceError}; this method tries to extract error
     * details if an exception is in that format.
     *
     * @param exception the exception from storage service
     * @return the extracted error as a string, null if the error cannot be parsed
     */
    static String tryGetNormalizedError(BlobStorageException exception) {
        return BlobServiceError.tryGetNormalizedError(exception);
    }

    @JacksonXmlRootElement(localName = "Error")
    private static class BlobServiceError {
        @JsonProperty("Code")
        private String code;

        @JsonProperty("Message")
        private String message;

        private static String tryGetNormalizedError(BlobStorageException exception) {
            Objects.requireNonNull(exception);
            final String rawMessage = exception.getServiceMessage();
            if (!CoreUtil.isNullOrEmpty(rawMessage)) {
                final SerializerAdapter serializer = SerializerAdapter.createDefault();
                try {
                    final BlobServiceError blobServiceError = serializer.deserialize(rawMessage,
                        BlobServiceError.class,
                        SerializerFormat.XML);
                    if (blobServiceError.code != null || blobServiceError.message != null) {
                        final StringBuilder builder = new StringBuilder();
                        builder.append(blobServiceError.code);
                        if (builder.length() > 0) {
                            builder.append(' ');
                        }
                        if (blobServiceError.message != null) {
                            builder.append(blobServiceError.message.replace("\n", " "));
                        }
                        return builder.toString();
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        }
    }
}
