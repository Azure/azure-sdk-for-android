// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.rest.annotation.Fluent;
import com.azure.core.serde.SerdeTypeInfo;
import com.azure.core.serde.SerdeTypeName;

/**
 * Defines a data change detection policy that captures changes using the
 * Integrated Change Tracking feature of Azure SQL Database.
 */
@SerdeTypeInfo(use = SerdeTypeInfo.Id.NAME, include = SerdeTypeInfo.As.PROPERTY, property = "@odata.type")
@SerdeTypeName("#Microsoft.Azure.Search.SqlIntegratedChangeTrackingPolicy")
@Fluent
public final class SqlIntegratedChangeTrackingPolicy extends DataChangeDetectionPolicy {
}
