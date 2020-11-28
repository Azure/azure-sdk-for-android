// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for describing any cognitive service resource attached to a
 * skillset.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = CognitiveServicesAccount.class)
@SerdeTypeName("CognitiveServicesAccount")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.DefaultCognitiveServices",
        value = DefaultCognitiveServicesAccount.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.CognitiveServicesByKey",
        value = CognitiveServicesAccountKey.class)
})
@Fluent
public abstract class CognitiveServicesAccount {
    /*
     * Description of the cognitive service resource attached to a skillset.
     */
    @SerdeProperty(value = "description")
    private String description;

    /**
     * Get the description property: Description of the cognitive service
     * resource attached to a skillset.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: Description of the cognitive service
     * resource attached to a skillset.
     *
     * @param description the description value to set.
     * @return the CognitiveServicesAccount object itself.
     */
    public CognitiveServicesAccount setDescription(String description) {
        this.description = description;
        return this;
    }
}
