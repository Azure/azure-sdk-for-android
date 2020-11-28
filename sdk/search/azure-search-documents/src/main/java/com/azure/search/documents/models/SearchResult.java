// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.logging.ClientLogger;
import com.azure.core.serde.SerdeIgnoreProperty;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeEncoding;
import com.azure.core.serde.jackson.JacksonSerderAdapter;
import com.azure.core.customserde.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.converters.SearchResultHelper;
import com.azure.core.serde.SerdeProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.azure.core.customserde.TypeReference.createInstance;
import static com.azure.search.documents.implementation.util.Utility.initializeSerdeAdapter;

/**
 * Contains a document found by a search query, plus associated metadata.
 */
@Fluent
public final class SearchResult {
    private final ClientLogger logger = new ClientLogger(SearchResult.class);

    /*
     * Unmatched properties from the message are deserialized this collection
     */
    @SerdeProperty(value = "")
    private SearchDocument additionalProperties;

    /*
     * The relevance score of the document compared to other documents returned
     * by the query.
     */
    @SerdeProperty(value = "@search.score")
    private double score;

    /*
     * Text fragments from the document that indicate the matching search
     * terms, organized by each applicable field; null if hit highlighting was
     * not enabled for the query.
     */
    @SerdeProperty(value = "@search.highlights")
    private Map<String, List<String>> highlights;

    @SerdeIgnoreProperty
    private JsonSerializer jsonSerializer;

    private static final JacksonSerderAdapter SEARCH_JACKSON_SERDER_ADAPTER = (JacksonSerderAdapter) initializeSerdeAdapter();

    static {
        SearchResultHelper.setAccessor(new SearchResultHelper.SearchResultAccessor() {
            @Override
            public void setAdditionalProperties(SearchResult searchResult, SearchDocument additionalProperties) {
                searchResult.setAdditionalProperties(additionalProperties);
            }

            @Override
            public void setHighlights(SearchResult searchResult, Map<String, List<String>> highlights) {
                searchResult.setHighlights(highlights);
            }

            @Override
            public void setJsonSerializer(SearchResult searchResult, JsonSerializer jsonSerializer) {
                searchResult.setJsonSerializer(jsonSerializer);
            }
        });
    }
    /**
     * Constructor of {@link SearchResult}.
     *
     * @param score The relevance score of the document compared to other documents returned
     * by the query.
     */
    @SerdeToPojo
    public SearchResult(
        @SerdeProperty(value = "@search.score")
            double score) {
        this.score = score;
    }
    /**
     * Get the additionalProperties property: Unmatched properties from the
     * message are deserialized this collection.
     *
     * @param modelClass The model class converts to.
     * @param <T> Convert document to the generic type.
     * @return the additionalProperties value.
     * @throws RuntimeException if there is IO error occurs.
     */
    public <T> T getDocument(Class<T> modelClass) {
        if (jsonSerializer == null) {
            try {
                String serializedJson = SEARCH_JACKSON_SERDER_ADAPTER.serialize(additionalProperties, SerdeEncoding.JSON);
                return SEARCH_JACKSON_SERDER_ADAPTER.deserialize(serializedJson, modelClass, SerdeEncoding.JSON);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new RuntimeException("Failed to deserialize search result.", ex));
            }
        }
        ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
        jsonSerializer.serialize(sourceStream, additionalProperties);
        return jsonSerializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()),
            createInstance(modelClass));
    }

    /**
     * Get the score property: The relevance score of the document compared to
     * other documents returned by the query.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Get the highlights property: Text fragments from the document that
     * indicate the matching search terms, organized by each applicable field;
     * null if hit highlighting was not enabled for the query.
     *
     * @return the highlights value.
     */
    public Map<String, List<String>> getHighlights() {
        return this.highlights;
    }

    /**
     * The private setter to set the additionalProperties property
     * via {@link SearchResultHelper.SearchResultAccessor}.
     *
     * @param additionalProperties The Unmatched properties from the message are deserialized this collection.
     */
    private void setAdditionalProperties(SearchDocument additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * The private setter to set the highlights property
     * via {@link SearchResultHelper.SearchResultAccessor}.
     *
     * @param highlights The Text fragments from the document that indicate the matching search terms.
     */
    private void setHighlights(Map<String, List<String>> highlights) {
        this.highlights = highlights;
    }

    /**
     * The private setter to set the jsonSerializer property
     * via {@link SearchResultHelper.SearchResultAccessor}.
     *
     * @param jsonSerializer The json serializer.
     */
    private void setJsonSerializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }
}
