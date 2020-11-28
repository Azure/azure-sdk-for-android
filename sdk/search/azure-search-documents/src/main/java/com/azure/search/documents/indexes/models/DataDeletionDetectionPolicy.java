// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeSubTypes;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Base type for data deletion detection policies.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type",
    defaultImpl = DataDeletionDetectionPolicy.class)
@SerdeTypeName("DataDeletionDetectionPolicy")
@SerdeSubTypes({
    @SerdeSubTypes.Type(name = "#Microsoft.Azure.Search.SoftDeleteColumnDeletionDetectionPolicy",
        value = SoftDeleteColumnDeletionDetectionPolicy.class)
})
@Fluent
public abstract class DataDeletionDetectionPolicy {
}
