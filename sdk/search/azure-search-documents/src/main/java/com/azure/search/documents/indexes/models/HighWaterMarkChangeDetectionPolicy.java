// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeToPojo;
import com.azure.core.serde.SerdeProperty;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a data change detection policy that captures changes based on the
 * value of a high water mark column.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.HighWaterMarkChangeDetectionPolicy")
@Fluent
public final class HighWaterMarkChangeDetectionPolicy extends DataChangeDetectionPolicy {
    /*
     * The name of the high water mark column.
     */
    @SerdeProperty(value = "highWaterMarkColumnName")
    private String highWaterMarkColumnName;

    /**
     * Constructor of {@link HighWaterMarkChangeDetectionPolicy}.
     *
     * @param highWaterMarkColumnName The name of the high water mark column.
     */
    @SerdeToPojo
    public HighWaterMarkChangeDetectionPolicy(
        @SerdeProperty(value = "highWaterMarkColumnName") String highWaterMarkColumnName) {
        this.highWaterMarkColumnName = highWaterMarkColumnName;
    }

    /**
     * Get the highWaterMarkColumnName property: The name of the high water
     * mark column.
     *
     * @return the highWaterMarkColumnName value.
     */
    public String getHighWaterMarkColumnName() {
        return this.highWaterMarkColumnName;
    }

}
