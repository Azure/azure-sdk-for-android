// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import java.util.List;

/**
 * Defines how the Suggest API should apply to a group of fields in the index.
 */
@Fluent
public final class SearchSuggester {
    /*
     * The name of the suggester.
     */
    @SerdeProperty(value = "name")
    private String name;

    /*
     * The list of field names to which the suggester applies. Each field must
     * be searchable.
     */
    @SerdeProperty(value = "sourceFields")
    private List<String> sourceFields;

    /**
     * Constructor of {@link SearchSuggester}.
     * @param name The name of the suggester.
     * @param sourceFields The list of field names to which the suggester applies. Each field must
     * be searchable.
     */
    @SerdeToPojo
    public SearchSuggester(
        @SerdeProperty(value = "name") String name,
        @SerdeProperty(value = "sourceFields") List<String> sourceFields) {
        this.name = name;
        this.sourceFields = sourceFields;
    }

    /**
     * Get the name property: The name of the suggester.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the sourceFields property: The list of field names to which the
     * suggester applies. Each field must be searchable.
     *
     * @return the sourceFields value.
     */
    public List<String> getSourceFields() {
        return this.sourceFields;
    }

}
