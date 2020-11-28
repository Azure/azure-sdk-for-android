// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for data change detection policies.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = DataChangeDetectionPolicy.class)
@SerdeTypeName("DataChangeDetectionPolicy")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.HighWaterMarkChangeDetectionPolicy",
        value = HighWaterMarkChangeDetectionPolicy.class),
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.SqlIntegratedChangeTrackingPolicy",
        value = SqlIntegratedChangeTrackingPolicy.class)
})
@Fluent
public abstract class DataChangeDetectionPolicy {
}
