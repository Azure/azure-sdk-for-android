package com.azure.data.model.partition

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

/**
 * This Annotation should be applied to any Document property that will function as a partition key.
 * This should correspond with the partition key field path (e.g. /user/id) used when creating a DocumentCollection
 */
@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class PartitionKey