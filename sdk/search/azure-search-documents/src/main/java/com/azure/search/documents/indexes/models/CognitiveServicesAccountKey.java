// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * A cognitive service resource provisioned with a key that is attached to a
 * skillset.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.CognitiveServicesByKey")
@Fluent
public final class CognitiveServicesAccountKey extends CognitiveServicesAccount {
    /*
     * The key used to provision the cognitive service resource attached to a
     * skillset.
     */
    @SerdeProperty(value = "key")
    private String key;

    /**
     * Constructor of {@link CognitiveServicesAccountKey}.
     *
     * @param key The key used to provision the cognitive service resource attached to a
     * skillset.
     */
    @SerdeToPojo
    public CognitiveServicesAccountKey(@SerdeProperty(value = "key") String key) {
        this.key = key;
    }

    /**
     * Get the key property: The key used to provision the cognitive service
     * resource attached to a skillset.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the key property: The key used to provision the cognitive service
     * resource attached to a skillset.
     *
     * @param key the key value to set.
     * @return the CognitiveServicesAccountKey object itself.
     */
    public CognitiveServicesAccountKey setKey(String key) {
        this.key = key;
        return this;
    }
}
