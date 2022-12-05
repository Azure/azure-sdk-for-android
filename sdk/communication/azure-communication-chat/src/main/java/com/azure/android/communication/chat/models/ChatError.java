// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.models;

import com.azure.android.core.rest.annotation.Immutable;

import java.util.List;

/** The Chat Services error. */
@Immutable
public final class ChatError {

    private final String code;

    private final String message;

    private final String target;

    private final List<ChatError> details;

    private final ChatError innerError;

    /**
     * Initializes a new instance of the ChatError class.
     * @param code The error code.
     * @param message The error message.
     * @param target The error target.
     * @param innerError The inner error if any.
     * @param details Further details about specific errors that led to this error.
     */
    public ChatError(String code, String message, String target, ChatError innerError, List<ChatError> details) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.innerError = innerError;
        this.details = details;
    }

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the details property: Further details about specific errors that led to this error.
     *
     * @return the details value.
     */
    public List<ChatError> getDetails() {
        return this.details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public ChatError getInnerError() {
        return this.innerError;
    }
}