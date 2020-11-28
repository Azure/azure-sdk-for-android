// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.search.documents.implementation.converters.IndexingResultHelper;
import com.azure.core.serde.SerdeProperty;

import java.io.Serializable;

/**
 * Status of an indexing operation for a single document.
 */
@Fluent
public final class IndexingResult implements Serializable {
    private static final long serialVersionUID = -8604424005271188140L;
    /*
     * The key of a document that was in the indexing request.
     */
    @SerdeProperty(value = "key")
    private String key;

    /*
     * The error message explaining why the indexing operation failed for the
     * document identified by the key; null if indexing succeeded.
     */
    @SerdeProperty(value = "errorMessage")
    private String errorMessage;

    /*
     * A value indicating whether the indexing operation succeeded for the
     * document identified by the key.
     */
    @SerdeProperty(value = "status")
    private boolean succeeded;

    /*
     * The status code of the indexing operation. Possible values include: 200
     * for a successful update or delete, 201 for successful document creation,
     * 400 for a malformed input document, 404 for document not found, 409 for
     * a version conflict, 422 when the index is temporarily unavailable, or
     * 503 for when the service is too busy.
     */
    @SerdeProperty(value = "statusCode")
    private int statusCode;

    static {
        IndexingResultHelper.setAccessor(new IndexingResultHelper.IndexingResultAccessor() {
            @Override
            public void setErrorMessage(IndexingResult indexingResult, String errorMessage) {
                indexingResult.setErrorMessage(errorMessage);
            }
        });
    }

    /**
     * Constructor of {@link IndexingResult}.
     *
     * @param key The key of a document that was in the indexing request.
     * @param succeeded The error message explaining why the indexing operation failed for the
     * document identified by the key; null if indexing succeeded.
     * @param statusCode The status code of the indexing operation. Possible values include: 200
     * for a successful update or delete, 201 for successful document creation,
     * 400 for a malformed input document, 404 for document not found, 409 for
     * a version conflict, 422 when the index is temporarily unavailable, or
     * 503 for when the service is too busy.
     */
    @SerdeToPojo
    public IndexingResult(
        @SerdeProperty(value = "key") String key,
        @SerdeProperty(value = "status") boolean succeeded,
        @SerdeProperty(value = "statusCode")
            int statusCode) {
        this.key = key;
        this.succeeded = succeeded;
        this.statusCode = statusCode;
    }

    /**
     * Get the key property: The key of a document that was in the indexing
     * request.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the errorMessage property: The error message explaining why the
     * indexing operation failed for the document identified by the key; null
     * if indexing succeeded.
     *
     * @return the errorMessage value.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Get the succeeded property: A value indicating whether the indexing
     * operation succeeded for the document identified by the key.
     *
     * @return the succeeded value.
     */
    public boolean isSucceeded() {
        return this.succeeded;
    }

    /**
     * Get the statusCode property: The status code of the indexing operation.
     * Possible values include: 200 for a successful update or delete, 201 for
     * successful document creation, 400 for a malformed input document, 404
     * for document not found, 409 for a version conflict, 422 when the index
     * is temporarily unavailable, or 503 for when the service is too busy.
     *
     * @return the statusCode value.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * The private setter to set the errorMessage property
     * via {@link IndexingResultHelper.IndexingResultAccessor}.
     *
     * @param errorMessage The reason for indexing operation failure.
     */
    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
